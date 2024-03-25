package com.homo.core.persistent.storage;

import brave.Span;
import com.homo.core.facade.storege.StorageDriver;
import com.homo.core.facade.storege.dirty.DirtyDriver;
import com.homo.core.facade.storege.dirty.DirtyHelper;
import com.homo.core.facade.storege.landing.DBDataHolder;
import com.homo.core.mysql.entity.DataObject;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.factory.RedisInfoHolder;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.lang.Pair;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.trace.TraceLogUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.charset.StandardCharsets;
import java.util.*;

;

/**
 * 设计思路
 * 1数据会按照指定格式进行存储到hSet中，该hSet称之为ownerKey,其中的filed称之为logicKey, 其中一个cachedAllKey（field）标识该key上的所有field都已加载进内存中
 * 存储流程：先存redis，再由另一台落地程序定时将redis上的数据落地到mysql
 * 存储规则： 当调用update或incr更新数据时，会在redis上打个不过期的string类型标签，标志该key
 * 存在在mysql（existKey）existKey用于判断数据是否是存在的
 * 2当调用get方法获取数据时，如果redis没有数据，会通过existKey判定是否要从mysql加载数据，
 * 3如果存在existKey,就进行数据加热操作（hotkey），不存在则直接返回
 * 4从mysql获取到数据后，会将其存入到redis中（数据结构是hSet），然后重新执行get方法从redis获取数据.
 * 与此同时会在数据结构上增加一个field标识(成员名为cacheKey)，cacheKey用于判断数据是否存在于redis，存在才会从redis获取数据
 * 如果cacheKey存在，就从redis捞取后返回，如果不存在，则执行步骤3
 * 5数据移除会将需要移除的数据迁移到hSet的另一个字段上（logicKey+:+del）（逻辑删除）,然后原先的logicKey的值会被打上删除标记（:delFlag标识）
 */
@Slf4j
public class MysqlRedisStorageDriverImpl implements StorageDriver {

    //    private static final String REDIS_KEY_TMPL = "slug-data:{%s:%s:%s:%s}";
    private static final String REDIS_EXIST_KEY_TMPL = "slug-exist:{%s:%s:%s:%s:exist}";

    @Autowired(required = false)
    @Qualifier("homoRedisPool")
    private HomoAsyncRedisPool redisPool;

    @Autowired(required = false)
    private DirtyDriver dirtyDriver;

    @Autowired(required = false)
    private RedisInfoHolder redisInfoHolder;

    @Autowired(required = false)
    private DBDataHolder<DataObject> DBDataHolder;

    @Override
    public Homo<Map<String, byte[]>> asyncGetByFields(String appId, String regionId, String logicType, String ownerId, List<String> fieldList) {
        log.trace("asyncGetByKeys start appId {} regionId {} logicType {} ownerId {}", appId, regionId, logicType, ownerId);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String existKey = String.format(REDIS_EXIST_KEY_TMPL, appId, regionId, logicType, ownerId);
        String queryFieldsScript = LuaScriptHelper.queryFieldsScript;
        String[] keys = {redisKey, existKey};
        byte[][] args = new byte[fieldList.size() + 1][];//expireTime + fieldList
        args[0] = redisInfoHolder.getExpireTime().toString().getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < fieldList.size(); i++) {
            args[i + 1] = fieldList.get(i).getBytes(StandardCharsets.UTF_8);
        }
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("asyncGetByFields").tag("type","storage") .annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(queryFieldsScript, keys, args);
        Homo<Map<String, byte[]>> warp = Homo.warp(homoSink -> {
            resultFlux.subscribe(ret -> {
                try {
                    TraceLogUtil.setTraceIdBySpan(span, "storage asyncGetByFields");
                    log.trace("asyncGetByFields subscribe appId {} regionId {} logicType {} ownerId {} fieldList {}", appId, regionId, logicType, ownerId, fieldList);
                    List arrayList = (ArrayList) ret;
                    Map<String, byte[]> map = new HashMap<>();
                    List needLoadFields = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(arrayList)) {
                        if (arrayList.size() == 1 && arrayList.get(0).equals(-1L)) {
                            homoSink.success(map);
                            return;
                        }
                        //将redis数据整合在map中
                        for (int i = 0; i < arrayList.size(); i += 2) {
                            if (arrayList.get(i + 1) != null) {
                                String field = new String((byte[]) arrayList.get(i), StandardCharsets.UTF_8);
                                if ("missNum".equals(field)) {
                                    if (!Collections.emptyList().equals(arrayList.get(i + 1))) {
                                        needLoadFields = (ArrayList) arrayList.get(i + 1);
                                    }
                                    continue;
                                }
                                byte[] bytes = (byte[]) arrayList.get(i + 1);
                                map.put(field, bytes);
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
                        DBDataHolder.hotFields(appId, regionId, logicType, ownerId, redisKey, queryFields)
                                .consumerValue(list -> {
                                    for (DataObject dataObject : list) {
                                        map.put(dataObject.getKey(), dataObject.getValue());
                                    }
                                    //redis取的数据是全的，直接返回
                                    log.info("asyncGetByFields hotFields appId {} regionId {} logicType {} ownerId {} fieldList {}", appId, regionId, logicType, ownerId, fieldList);
                                    span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                                    homoSink.success(map);
                                }).start();
                    } else {
                        //redis取的数据是全的，直接返回
                        log.info("asyncGetByFields complete appId {} regionId {} logicType {} ownerId {} fieldList {}", appId, regionId, logicType, ownerId, fieldList);
                        span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                        homoSink.success(map);
                    }
                } catch (Exception e) {
                    span.error(e);
                    homoSink.error(e);
                }
            }, homoSink::error);
        });
        return warp.switchThread(callQueue,span);
    }

    @Override
    public Homo<Map<String, byte[]>> asyncGetAll(String appId, String regionId, String logicType, String ownerId) {
        log.trace("asyncGetAll start appId {}, regionId {}, logicType {}, ownerId {}", appId, regionId, logicType, ownerId);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String persistenceKey = String.format(REDIS_EXIST_KEY_TMPL, appId, regionId, logicType, ownerId);
        String queryAllKeyScript = LuaScriptHelper.queryAllFieldsScript;
        String[] keys = {redisKey, persistenceKey};
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(queryAllKeyScript, keys, redisInfoHolder.getExpireTime().toString().getBytes(StandardCharsets.UTF_8));
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("asyncGetAll").tag("type","storage").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Homo<Map<String, byte[]>> warp = Homo.warp(homoSink -> {
            resultFlux.subscribe(ret -> {
                try {
                    TraceLogUtil.setTraceIdBySpan(span, "storage asyncGetAll");
                    ArrayList list = (ArrayList) ret;
                    Map<String, byte[]> map = new HashMap<>();
                    if (list.size() == 1 && list.get(0).equals(0L)) {//数据库里有数据但内存里的数据不是最新的
                        DBDataHolder.hotAllField(appId, regionId, logicType, ownerId, redisKey)
                                .consumerValue(bool -> {
                                    //重新从redis拿
                                    asyncGetAll(appId, regionId, logicType, ownerId).start();
                                    span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                                    homoSink.success(new HashMap<>());
                                });
                        return;
                    }
                    if (!(list.size() == 1 && list.get(0).equals(-1L))) {//如果返回的是-1，即没有全量的key，跳过整合map的步骤
                        log.trace("asyncGetAll load from redis success appId {} regionId {} logicType {} ownerId {}", appId, regionId, logicType, ownerId);
                        for (int i = 0; i < list.size(); i += 2) {
                            if (list.get(i + 1) != null) {
                                String filed = new String((byte[]) list.get(i), StandardCharsets.UTF_8);
                                byte[] bytes = (byte[]) list.get(i + 1);
                                map.put(filed, bytes);
                            }
                        }
                    }
                    span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                    homoSink.success(map);
                } catch (Exception exception) {
                    span.error(exception);
                    homoSink.error(exception);
                }
            }, homoSink::error);
        });
        return warp.switchThread(callQueue,span);
    }


    @Override
    public Homo<Pair<Boolean, Map<String, byte[]>>> asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data) {
        log.trace("asyncUpdate start appId {} regionId {} logicType {} ownerId {}", appId, regionId, logicType, ownerId);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String existKey = String.format(REDIS_EXIST_KEY_TMPL, appId, regionId, logicType, ownerId);
        String updateFieldsScript = LuaScriptHelper.updateFieldsScript;
        String[] keys = {redisKey, existKey};
        byte[][] args = new byte[data.size() * 2 + 1][];//expireTime:field1,value1:field2,value2:field3...
        args[0] = redisInfoHolder.getExpireTime().toString().getBytes(StandardCharsets.UTF_8);
        int index = 1;
        DirtyHelper dirtyHelper = DirtyHelper.create(redisKey);
        for (Map.Entry<String, byte[]> dataEntry : data.entrySet()) {
            String field = dataEntry.getKey();
            args[index] = field.getBytes(StandardCharsets.UTF_8);
            args[index + 1] = dataEntry.getValue();
            index += 2;
            dirtyHelper.update(appId, regionId, logicType, ownerId, field);
        }
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("asyncUpdate").tag("type","storage").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        log.trace("asyncUpdate exec appId {} regionId {} logicType {} ownerId {} keys {} args {}", appId, regionId, logicType, ownerId, keys, args);
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(updateFieldsScript, keys, args);
        Homo<Pair<Boolean, Map<String, byte[]>>> warp = Homo.warp(homoSink -> {
            resultFlux.subscribe(ret -> {
                try {
                    TraceLogUtil.setTraceIdBySpan(span, "storage asyncUpdate");
                    log.trace("asyncUpdate finish appId {} regionId {} logicType {} ownerId {} ret {}", appId, regionId, logicType, ownerId, ret);
                    dirtyDriver.dirtyUpdate(dirtyHelper.build())
                            .consumerValue(res -> {
                                Pair<Boolean, Map<String, byte[]>> pair = new Pair<>(true, new HashMap<>());
                                span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                                homoSink.success(pair);
                            }).start();
                } catch (Exception e) {
                    span.error(e);
                    homoSink.error(e);
                }
            }, homoSink::error);
        });
        return warp.switchThread(callQueue,span);
    }

    @Override
    public Homo<Pair<Boolean, Map<String, Long>>> asyncIncr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData) {
        log.trace("asyncIncr start appId {} regionId {} logicType {} ownerId {} incrData {}", appId, regionId, logicType, ownerId, incrData);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String existKey = String.format(REDIS_EXIST_KEY_TMPL, appId, regionId, logicType, ownerId);
        String asyncIncrScript = LuaScriptHelper.asyncIncrScript;
        String[] keys = {redisKey, existKey};
        String[] args = new String[incrData.size() * 2 + 1];//expireTime:incrKey1,value1:incrKey2,value2:incrKey3...
        args[0] = redisInfoHolder.getExpireTime().toString();
        int index = 1;
        for (Map.Entry<String, Long> dataEntry : incrData.entrySet()) {
            String incrKey = dataEntry.getKey();
            args[index] = incrKey;
            args[index + 1] = String.valueOf(dataEntry.getValue());
            index += 2;
        }
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("asyncIncr").tag("type","storage").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(asyncIncrScript, keys, args);
        Homo<Pair<Boolean, Map<String, Long>>> warp = Homo.warp(homoSink -> {
            resultFlux.subscribe(ret -> {
                try {
                    TraceLogUtil.setTraceIdBySpan(span, "storage asyncIncr");
                    log.trace("asyncIncr subscribe appId {} regionId {} logicType {} ownerId {} result {}", appId, regionId, logicType, ownerId, ret);
                    ArrayList list = (ArrayList) ret;
                    Map<String, Long> retMap = new HashMap<>();
                    Pair<Boolean, Map<String, Long>> pair = new Pair<>(true, retMap);
                    if (!CollectionUtils.isEmpty(list)) {
                        if (list.size() == 1 && list.get(0).equals("unCachedAllKey")) {
                            DBDataHolder.hotAllField(appId, regionId, logicType, ownerId, redisKey)
                                    .consumerValue(res ->
                                            asyncIncr(appId, regionId, logicType, ownerId, incrData)
                                                    .consumerValue(res2 -> {
                                                                span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                                                                homoSink.success(res2);
                                                            }
                                                    ).start()
                                    )
                                    .catchError(homoSink::error).start();
                            return;
                        }
                        DirtyHelper dirtyHelper = DirtyHelper.create(redisKey);
                        for (int i = 0; i < list.size(); i += 2) {
                            retMap.put((String) list.get(i), (Long) list.get(i + 1));
                            dirtyHelper.incr(appId, regionId, logicType, ownerId, (String) list.get(i), (Long) list.get(i + 1));
                        }

                        dirtyDriver.dirtyUpdate(dirtyHelper.build())
                                .consumerValue(res -> {
                                    log.trace("asyncIncr complete appId {} regionId {} logicType {} ownerId {} incrData {}", appId, regionId, logicType, ownerId, ret);
                                    span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                                    homoSink.success(pair);
                                }).start();
                    }
                } catch (Exception e) {
                    span.error(e);
                    homoSink.error(e);
                }
            }, homoSink::error);
        });
        return warp.switchThread(callQueue,span);
    }

    @Override
    public Homo<Boolean> asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys) {
        log.trace("asyncRemoveKeys start, appId {} regionId {} logicType {} ownerId {} keys {}", appId, regionId, logicType, ownerId, remKeys);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String removeFieldsScript = LuaScriptHelper.removeFieldsScript;
        String[] keys = {redisKey};
        String[] args = new String[remKeys.size() + 1];//expireTime:field1:field2:field3...
        args[0] = redisInfoHolder.getExpireTime().toString();
        int index = 1;
        DirtyHelper dirtyHelper = DirtyHelper.create(redisKey);

        for (String remField : remKeys) {
            args[index] = remField;
            index += 1;
            dirtyHelper.remove(appId, regionId, logicType, ownerId, remField);
        }
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("asyncRemoveKeys").tag("type","storage").annotate(ZipkinUtil.CLIENT_SEND_TAG);
        Flux<Object> resultFlux = redisPool.evalAsyncReactive(removeFieldsScript, keys, args);
        Homo<Boolean> warp = Homo.warp(homoSink -> {
            resultFlux.subscribe(ret -> {
                TraceLogUtil.setTraceIdBySpan(span, "storage asyncRemoveKeys");
                log.trace("asyncRemoveKeys subscribe appId {} regionId {} logicType {} ownerId {} keys {}", appId, regionId, logicType, ownerId, remKeys);
                ArrayList list = (ArrayList) ret;
                if (list.size() == 1 && list.get(0).equals("unCachedAllKey")) {
                    DBDataHolder
                            .hotAllField(appId, regionId, logicType, ownerId, redisKey)
                            .nextDo(res ->
                                    asyncRemoveKeys(appId, regionId, logicType, ownerId, remKeys)
                                            .consumerValue(v -> {
                                                span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                                                homoSink.success(v);
                                            })
                            )
                            .start();
                } else {
                    dirtyDriver.dirtyUpdate(dirtyHelper.build())
                            .consumerValue(res -> {
                                span.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                                homoSink.success(true);
                            }).start();
                }
            }, homoSink::error);
        });
        return warp.switchThread(callQueue,span);
    }
}
