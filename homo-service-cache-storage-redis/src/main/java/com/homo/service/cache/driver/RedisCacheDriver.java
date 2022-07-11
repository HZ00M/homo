package com.homo.service.cache.driver;

import com.google.common.base.Charsets;
import com.homo.core.facade.cache.CacheDriver;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.lang.Pair;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;

@Log4j2
@Component
public class RedisCacheDriver implements CacheDriver {
    private static String REDIS_KEY_TMPL = "slug:{%s:%s:%s:%s}";  //slug:{appId:regionId:logicType:ownerId}

    @Autowired
    HomoAsyncRedisPool asyncRedisPool;
    @Autowired
    LuaScriptHelper luaScriptHelper;

    @Override
    public void asyncGetByFields(String appId, String regionId, String logicType, String ownerId, List<String> fieldList, CallBack<Map<String, byte[]>> callBack) {
        log.trace("asyncGet start, appId_{} regionId_{} logicType_{} ownerId_{}, keyList_{}", appId, regionId, logicType, ownerId, fieldList);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        byte[][] fields = new byte[fieldList.size()][];
        int index = 0;
        for (String key : fieldList) {
            fields[index++] = key.getBytes(StandardCharsets.UTF_8);
        }
        RedisFuture<List<KeyValue<byte[], byte[]>>> listRedisFuture = asyncRedisPool.hmgetAsync(redisKey.getBytes(StandardCharsets.UTF_8), fields);
        listRedisFuture.whenCompleteAsync((result, throwable) -> {
            try {
                if (throwable != null) {
                    callBack.onError(throwable);
                    return;
                }
                Map<String, byte[]> resultMap = new HashMap<>(result.size());
                for (KeyValue<byte[], byte[]> keyValue : result) {
                    if (!keyValue.hasValue()) {
                        continue;
                    }
                    resultMap.put(new String(keyValue.getKey(), StandardCharsets.UTF_8), keyValue.getValue());
                }
                callBack.onBack(resultMap);
                log.info("asyncGet end, appId_{} regionId_{} logicType_{} ownerId_{}, keyList_{}", appId, regionId, logicType, ownerId, fieldList);
            } catch (Exception e) {
                callBack.onError(e);
            }
        });
    }

    @Override
    public void asyncGetAll(String appId, String regionId, String logicType, String ownerId, CallBack<Map<String, byte[]>> callBack) {
        log.trace("asyncGet start,appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        RedisFuture<Map<byte[], byte[]>> mapRedisFuture = asyncRedisPool.hgetallAsync(redisKey.getBytes(StandardCharsets.UTF_8));
        mapRedisFuture.whenCompleteAsync((result, throwable) -> {
                    try {
                        if (throwable != null) {
                            callBack.onError(throwable);
                            return;
                        }
                        Map<String, byte[]> resultMap = new HashMap<>(result.size());
                        for (byte[] keyBytes : result.keySet()) {
                            String key = new String(keyBytes, StandardCharsets.UTF_8);
                            resultMap.put(key, result.get(keyBytes));
                        }
                        callBack.onBack(resultMap);
                        log.trace("asyncGet end,appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
                    } catch (Exception e) {
                        callBack.onError(e);
                    }
                }
        );
    }

    @Override
    public void asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data, CallBack<Boolean> callBack) {
        log.trace("asyncUpdate start, appId_{} regionId_{} logicType_{} ownerId_{}, keys_{}", appId, regionId, logicType, ownerId, data.keySet());
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        Map<byte[], byte[]> dataMap = new HashMap<>(data.size());
        for (String key : data.keySet()) {
            byte[] keyBytes = key.getBytes(Charsets.UTF_8);
            dataMap.put(keyBytes, data.get(key));
        }
        RedisFuture<Long> longRedisFuture = asyncRedisPool.hsetAsync(redisKey.getBytes(StandardCharsets.UTF_8), dataMap);
        longRedisFuture.whenCompleteAsync((result, throwable) -> {
            try {
                if (throwable != null) {
                    callBack.onError(throwable);
                    return;
                }
                //hset方法如果原来没这个field返回 1，如果存在，则是覆盖旧值，返回 0
                callBack.onBack(Boolean.TRUE);
                log.trace("asyncUpdate end, appId_{} regionId_{} logicType_{} ownerId_{}, keys_{}", appId, regionId, logicType, ownerId, data.keySet());
            } catch (Exception e) {
                callBack.onError(e);
            }
        });
    }

    @Override
    public void asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data, long expireSeconds, CallBack<Boolean> callBack) {
        log.trace("asyncUpdate start, appId_{} regionId_{} logicType_{} ownerId_{} expireSeconds_{}", appId, regionId, logicType, ownerId, expireSeconds);
        if (expireSeconds <= 0) {
            //没有超时时间
            asyncUpdate(appId, regionId, logicType, ownerId, data, callBack);
        } else {
            String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
            String[] keys = {redisKey};
            byte[][] args = new byte[data.size() * 2 + 1][];
            args[0] = String.valueOf(expireSeconds).getBytes(StandardCharsets.UTF_8);
            int index = 1;
            for (Map.Entry<String, byte[]> entry : data.entrySet()) {
                args[index] = entry.getKey().getBytes(StandardCharsets.UTF_8);
                args[index + 1] = entry.getValue();
                index += 2;
            }
            String updateKeysExpireScript = LuaScriptHelper.updateKeysExpireScript;
            Flux<Object> resultFlux = asyncRedisPool.evalAsyncReactive(updateKeysExpireScript, keys, args);
            resultFlux.subscribe(
                    result -> {
                        try {
                            callBack.onBack(Boolean.TRUE);
                            log.trace("asyncUpdate end, appId_{} regionId_{} logicType_{} ownerId_{} expireSeconds_{}", appId, regionId, logicType, ownerId, expireSeconds);
                        } catch (Exception e) {
                            callBack.onError(e);
                        }
                    },
                    callBack::onError
            );
        }
    }

    @Override
    public void asyncIncr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData, CallBack<Pair<Boolean, Map<String, Long>>> callBack) {
        log.trace("asyncIncr start, appId_{} regionId_{} logicType_{} ownerId_{} incrKeys_{}", appId, regionId, logicType, ownerId, incrData.keySet());
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        String incrScript = LuaScriptHelper.incrScript;
        String[] keys = new String[1];
        String[] args = new String[incrData.size() * 2];
        keys[0] = redisKey;
        int index = 0;
        for (String key : incrData.keySet()) {
            args[index] = key;
            args[index + 1] = String.valueOf(incrData.get(key));
            index += 2;
        }
        RedisFuture<Object> objectRedisFuture = asyncRedisPool.evalAsync(incrScript, keys, args);
        objectRedisFuture.whenCompleteAsync((result, throwable) -> {
            try {
                if (throwable != null) {
                    callBack.onError(throwable);
                    return;
                }
                ArrayList resultList = (ArrayList) result;
                Map<String, Long> resultMap = new HashMap<>(resultList.size() / 2);
                for (int i = 0; i < resultList.size(); i += 2) {
                    resultMap.put((String) resultList.get(i), (Long) resultList.get(i + 1));
                }
                Pair<Boolean, Map<String, Long>> pair = new Pair<>(true, resultMap);
                callBack.onBack(pair);
                log.info("asyncIncr end, appId_{} regionId_{} logicType_{} ownerId_{} incrKeys_{}", appId, regionId, logicType, ownerId, incrData.keySet());
            } catch (Exception e) {
                callBack.onError(e);
            }
        });
    }

    @Override
    public void asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys, CallBack<Boolean> callBack) {
        log.trace("asyncRemoveKeys start, appId_{} regionId_{} logicType_{} ownerId_{} keys_{}", appId, regionId, logicType, ownerId, remKeys);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        byte[][] keys = new byte[remKeys.size()][];
        int index = 0;
        for (String key : remKeys) {
            keys[index++] = key.getBytes(StandardCharsets.UTF_8);
        }
        RedisFuture<Long> longRedisFuture = asyncRedisPool.hdelAsync(redisKey.getBytes(StandardCharsets.UTF_8), keys);
        longRedisFuture.whenCompleteAsync((result,throwable)->{
            try {
                if (throwable!=null){
                    callBack.onError(throwable);
                    return;
                }
                //返回值:被成功移除的域的数量，不包括被忽略的域。
                callBack.onBack(true);
                log.trace("asyncRemoveKeys end, appId_{} regionId_{} logicType_{} ownerId_{} keys_{}", appId, regionId, logicType, ownerId, remKeys);
            }catch (Exception e){
                callBack.onError(e);
            }
        });
    }
}
