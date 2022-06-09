package com.homo.core.persisten.storage;

import com.homo.core.common.pojo.DataObject;
import com.homo.core.facade.storege.DirtyDriver;
import com.homo.core.facade.storege.StorageDriver;
import com.homo.core.persisten.handler.LoadDataHolder;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.factory.RedisInfoHolder;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.lang.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 设计思路
 * 1数据会按照指定格式进行存储到hSet中，该hSet称之为ownerKey,其中的filed称之为logicKey, 其中一个cachedAllKey（field）标识该key上的所有field都已加载进内存中
 * 存储流程：先存redis，再由另一台落地程序定时将redis上的数据落地到mysql
 * 存储规则： 当调用update或incr更新数据时，会在redis上打个不过期的string类型标签，标志该key
 * 存在在mysql（persistenceKey）persistenceKey用于判断数据是否存在于mysql
 * 2当调用get方法获取数据时，如果redis没有数据，会通过persistenceKey判定mysql是否存在该数据，
 * 3如果存在persistenceKey,就进行数据加热操作（hotkey），不存在则直接返回
 * 4从mysql获取到数据后，会将其存入到redis中（数据结构是hSet），然后重新执行get方法从redis获取数据.
 * 与此同时会在数据结构上增加一个field标识(成员名为cacheKey)，cacheKey用于判断数据是否存在于redis，存在才会从redis获取数据
 * 如果cacheKey存在，就从redis捞取后返回，如果不存在，则执行步骤3
 * 5数据移除会将需要移除的数据迁移到hSet的另一个字段上（logicKey+:+del）（逻辑删除）,然后原先的logicKey的值会被打上删除标记（delFlag标识）
 */
@Slf4j
public class StorageRedisDriverImpl implements StorageDriver {

    private static final String REDIS_KEY_TMPL = "slug:{%s:%s:%s:%s}";
    private static final String REDIS_PERSISTENCE_KEY_TMPL = "persistence:{%s:%s:%s:%s}";
    private static final String REDIS_LOCK_TMPL = "lock:{%s:%s:%s:%s}:%s";

    @Autowired
    private HomoAsyncRedisPool redisPool;

    @Autowired
    private LuaScriptHelper luaScriptHelper;

    @Autowired
    private DirtyDriver dirtyDriver;

    @Autowired
    private RedisInfoHolder redisInfoHolder;

    @Autowired
    private LoadDataHolder loadDataHolder;

    @Override
    public void asyncGetByFields(String appId, String regionId, String logicType, String ownerId, List<String> fieldList, CallBack<Map<String, byte[]>> callBack) {
        log.trace("asyncGetByKeys start appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
        String key = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String persistenceKey = String.format(REDIS_PERSISTENCE_KEY_TMPL, appId, regionId, logicType, ownerId);
        String queryKeysScript = luaScriptHelper.getQueryFieldsScript();
        String[] keys = {key, persistenceKey};
        byte[][] args = new byte[fieldList.size() + 1][];//expireTime + fieldList
        args[0] = redisInfoHolder.getExpireTime().toString().getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < fieldList.size(); i++) {
            args[i + 1] = fieldList.get(i).getBytes(StandardCharsets.UTF_8);
        }
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(queryKeysScript, keys, args);
        resultFlux.subscribe(ret -> {
            try {
                log.trace("asyncGetByFields subscribe appId_{} regionId_{} logicType_{} ownerId_{} fieldList_{}", appId, regionId, logicType, ownerId, fieldList);
                List arrayList = (ArrayList) ret;
                Map<String, byte[]> map = new HashMap<>();
                List needLoadFields = new ArrayList<>();
                if (!CollectionUtils.isEmpty(arrayList)) {
                    if (arrayList.size() == 1 && arrayList.get(0).equals(-1)) {
                        callBack.onBack(map);
                        return;
                    }
                    //将redis数据整合在map中
                    for (int i = 0; i < arrayList.size(); i += 2) {
                        if (arrayList.get(i + 1) != null) {
                            String field = new String((byte[]) arrayList.get(i), StandardCharsets.UTF_8);
                            if ("needLoadFlag".equals(field)) {
                                if (!Collections.emptyList().equals(arrayList.get(i + 1))) {
                                    needLoadFields = (ArrayList) arrayList.get(i + 1);
                                }
                                continue;
                            }
                            byte[] bytes = (byte[]) arrayList.get(i + 1);
                            map.put(key, bytes);
                        }
                    }
                }
                //如果redis取的数据不全，从数据库中获取
                if (needLoadFields.size() > 0) {
                    List<String> queryFields = new ArrayList<>();
                    //构造MySQL查询的key
                    for (Object needLoadField : needLoadFields) {
                        queryFields.add(new String((byte[]) needLoadField));
                    }
                    //查询数据库
                    loadDataHolder.hotFields(appId, regionId, logicType, ownerId, key, queryFields, new CallBack<List<DataObject>>() {
                        @Override
                        public void onBack(List<DataObject> list) {
                            for (DataObject dataObject : list) {
                                map.put(dataObject.getKey(), dataObject.getValue());
                            }
                            callBack.onBack(map);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            callBack.onError(throwable);
                        }
                    });
                    return;
                }
                //redis取的数据是全的，直接返回
                callBack.onBack(map);
                log.trace("asyncGetByFields subscribe complete appId_{} regionId_{} logicType_{} ownerId_{} fieldList_{}", appId, regionId, logicType, ownerId, fieldList);
            } catch (Exception e) {
                callBack.onError(e);
            }
        }, callBack::onError);

    }

    @Override
    public void asyncGetAll(String appId, String regionId, String logicType, String ownerId, CallBack<Map<String, byte[]>> callBack) {
        log.trace("asyncGetAll start appId_{}, regionId_{}, logicType_{}, ownerId_{}", appId, regionId, logicType, ownerId);
        String key = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String persistenceKey = String.format(REDIS_PERSISTENCE_KEY_TMPL, appId, regionId, logicType, ownerId);
        String queryAllKeyScript = luaScriptHelper.getQueryAllFieldsScript();
        String[] keys = {key, persistenceKey};
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(queryAllKeyScript, keys, redisInfoHolder.getExpireTime().toString().getBytes(StandardCharsets.UTF_8));
        resultFlux.subscribe(ret -> {
            try {
                ArrayList list = (ArrayList) ret;
                Map<String, byte[]> map = new HashMap<>();

                if (list.size() == 1 && list.get(0).equals(0L)) {//数据库里有数据但内存里的数据不是最新的
                    loadDataHolder.hotAllField(appId, regionId, logicType, ownerId, key, new CallBack<Boolean>() {
                        @Override
                        public void onBack(Boolean value) {
                            log.trace("asyncGetAll load from mysql success appId_{} regionId_{} logicType_{} ownerId_{} result_{}", appId, regionId, logicType, ownerId, value);
                            //重新从redis拿
                            asyncGetAll(appId, regionId, logicType, ownerId, callBack);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            callBack.onError(throwable);
                        }
                    });
                    return;
                }
                if (!(list.size() == 1 && list.get(0).equals(-1L))) {//如果返回的是-1，即没有全量的key，跳过整合map的步骤
                    log.trace("asyncGetAll load from redis success appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
                    for (int i = 0; i < list.size(); i += 2) {
                        if (list.get(i + 1) != null) {
                            String filed = new String((byte[]) list.get(i), StandardCharsets.UTF_8);
                            byte[] bytes = (byte[]) list.get(i + 1);
                            map.put(filed, bytes);
                        }
                    }
                }
                callBack.onBack(map);
            } catch (Exception exception) {
                callBack.onError(exception);
            }
        }, callBack::onError);
    }


    @Override
    public void asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data, CallBack<Pair<Boolean, Map<String, byte[]>>> callBack) {
        log.trace("asyncUpdate start appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
        String key = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String persistenceKey = String.format(REDIS_PERSISTENCE_KEY_TMPL, appId, regionId, logicType, ownerId);
        String updateFieldsScript = luaScriptHelper.getUpdateFieldsScript();
        String[] keys = {key, persistenceKey};
        byte[][] args = new byte[data.size() * 2 + 1][];//expireTime:field1,value1:field2,value2:field3...
        args[0] = redisInfoHolder.getExpireTime().toString().getBytes(StandardCharsets.UTF_8);
        int index = 1;
        for (Map.Entry<String, byte[]> dataEntry : data.entrySet()) {
            String field = dataEntry.getKey();
            args[index] = field.getBytes(StandardCharsets.UTF_8);
            args[index + 1] = dataEntry.getValue();
            index += 2;
        }
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(updateFieldsScript, keys, args);
        resultFlux.subscribe(ret -> {
            try {
                log.trace("asyncUpdate finish appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
            } catch (Exception e) {
                callBack.onError(e);
            }
        }, callBack::onError);
    }

    @Override
    public void asyncIncr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData, CallBack<Pair<Boolean, Map<String, Long>>> callBack) {
        log.trace("asyncIncr start appId_{} regionId_{} logicType_{} ownerId_{} incrData_{}", appId, regionId, logicType, ownerId, incrData);
        String key = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String persistenceKey = String.format(REDIS_PERSISTENCE_KEY_TMPL, appId, regionId, logicType, ownerId);
        String asyncIncrScript = luaScriptHelper.getAsyncIncrScript();
        String[] keys = {key, persistenceKey};
        String[] args = new String[incrData.size() * 2 + 1];//expireTime:incrKey1,value1:incrKey2,value2:incrKey3...
        args[0] = redisInfoHolder.getExpireTime().toString();
        int index = 1;
        for (Map.Entry<String, Long> dataEntry : incrData.entrySet()) {
            String incrKey = dataEntry.getKey();
            args[index] = incrKey;
            args[index + 1] = String.valueOf(dataEntry.getValue());
            index += 2;
        }
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(asyncIncrScript, keys, args);
        resultFlux.subscribe(ret -> {
            try {
                log.trace("asyncIncr subscribe appId_{} regionId_{} logicType_{} ownerId_{} result_{}", appId, regionId, logicType, ownerId, ret);
                ArrayList list = (ArrayList) ret;
                Map<String, Long> retMap = new HashMap<>();
                Pair<Boolean, Map<String, Long>> pair = new Pair<>(true, retMap);
                if (!CollectionUtils.isEmpty(list)) {
                    if (list.size() == 1 && list.get(0).equals("unCachedAllKey")) {
                        loadDataHolder.hotAllField(appId, regionId, logicType, ownerId, key, new CallBack<Boolean>() {
                            @Override
                            public void onBack(Boolean value) {
                                asyncIncr(appId, regionId, logicType, ownerId, incrData, callBack);
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                callBack.onError(throwable);
                            }
                        });
                        return;
                    }
                    for (int i = 0; i < list.size(); i += 2) {
                        retMap.put((String) list.get(i), (Long) list.get(i + 1));
                        callBack.onBack(pair);
                    }
                    log.trace("asyncIncr complete appId_{} regionId_{} logicType_{} ownerId_{} incrData_{}", appId, regionId, logicType, ownerId, ret);
                }
            } catch (Exception e) {
                callBack.onError(e);
            }
        }, callBack::onError);
    }

    @Override
    public void asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys, CallBack<Boolean> callBack) {
        log.trace("asyncRemoveKeys start, appId_{} regionId_{} logicType_{} ownerId_{} keys_{}", appId, regionId, logicType, ownerId, remKeys);
        String key = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String removeFieldsScript = luaScriptHelper.getRemoveFieldsScript();
        String[] keys = {key};
        String[] args = new String[remKeys.size() + 1];//expireTime:field1:field2:field3...
        args[0] = redisInfoHolder.getExpireTime().toString();
        int index = 1;
        for (String remField : remKeys) {
            args[index] = remField;
            index += 1;
        }
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(removeFieldsScript, keys, args);
        resultFlux.subscribe(ret -> {
            log.trace("asyncRemoveKeys subscribe appId_{} regionId_{} logicType_{} ownerId_{} keys_{}", appId, regionId, logicType, ownerId, remKeys);
            ArrayList list = (ArrayList) ret;
            if (list.size() == 1 && list.get(0).equals("unCachedAllKey")) {
                loadDataHolder.hotAllField(appId, regionId, logicType, ownerId, key, new CallBack<Boolean>() {
                    @Override
                    public void onBack(Boolean value) {
                        asyncRemoveKeys(appId, regionId, logicType, ownerId, remKeys, callBack);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callBack.onError(throwable);
                    }
                });
                return;
            }
            callBack.onBack(true);
        }, callBack::onError);
    }
}
