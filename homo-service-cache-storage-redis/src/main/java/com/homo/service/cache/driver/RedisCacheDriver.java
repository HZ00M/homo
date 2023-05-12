package com.homo.service.cache.driver;

import com.google.common.base.Charsets;
import com.homo.core.facade.cache.CacheDriver;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.lang.Pair;
import com.homo.core.utils.rector.Homo;
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
public class RedisCacheDriver implements CacheDriver {
    private static String REDIS_KEY_TMPL = "slug-cache:{%s:%s:%s:%s}";  //slug:{appId:regionId:logicType:ownerId}
    @Autowired
    HomoAsyncRedisPool asyncRedisPool;

    @Override
    public Homo<Map<String, byte[]>> asyncGetByFields(String appId, String regionId, String logicType, String ownerId, List<String> fieldList) {
        log.trace("asyncGet start, appId_{} regionId_{} logicType_{} ownerId_{}, keyList_{}", appId, regionId, logicType, ownerId, fieldList);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        byte[][] fields = new byte[fieldList.size()][];
        int index = 0;
        for (String key : fieldList) {
            fields[index++] = key.getBytes(StandardCharsets.UTF_8);
        }
        RedisFuture<List<KeyValue<byte[], byte[]>>> listRedisFuture = asyncRedisPool.hmgetAsync(redisKey.getBytes(StandardCharsets.UTF_8), fields);
        return Homo.warp(homoSink -> {
            listRedisFuture.whenCompleteAsync((result, throwable) -> {
                try {
                    if (throwable != null) {
                        homoSink.error(throwable);
                        return;
                    }
                    Map<String, byte[]> resultMap = new HashMap<>(result.size());
                    for (KeyValue<byte[], byte[]> keyValue : result) {
                        if (!keyValue.hasValue()) {
                            continue;
                        }
                        resultMap.put(new String(keyValue.getKey(), StandardCharsets.UTF_8), keyValue.getValue());
                    }
                    homoSink.success(resultMap);
                    log.info("asyncGet end, appId_{} regionId_{} logicType_{} ownerId_{}, keyList_{}", appId, regionId, logicType, ownerId, fieldList);
                } catch (Exception e) {
                    homoSink.error(e);
                }
            });
        });
    }

    @Override
    public Homo<Map<String, byte[]>> asyncGetAll(String appId, String regionId, String logicType, String ownerId) {
        log.trace("asyncGet start,appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        RedisFuture<Map<byte[], byte[]>> mapRedisFuture = asyncRedisPool.hgetallAsync(redisKey.getBytes(StandardCharsets.UTF_8));
        return Homo.warp(mapHomoSink -> {
            mapRedisFuture.whenCompleteAsync((result, throwable) -> {
                        try {
                            if (throwable != null) {
                                mapHomoSink.error(throwable);
                                return;
                            }
                            Map<String, byte[]> resultMap = new HashMap<>(result.size());
                            for (byte[] keyBytes : result.keySet()) {
                                String key = new String(keyBytes, StandardCharsets.UTF_8);
                                resultMap.put(key, result.get(keyBytes));
                            }
                            mapHomoSink.success(resultMap);
                            log.trace("asyncGet end,appId_{} regionId_{} logicType_{} ownerId_{}", appId, regionId, logicType, ownerId);
                        } catch (Exception e) {
                            mapHomoSink.error(e);
                        }
                    }
            );
        });
    }

    @Override
    public Homo<Boolean> asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data) {
        log.trace("asyncUpdate start, appId_{} regionId_{} logicType_{} ownerId_{}, keys_{}", appId, regionId, logicType, ownerId, data.keySet());
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        Map<byte[], byte[]> dataMap = new HashMap<>(data.size());
        for (String key : data.keySet()) {
            byte[] keyBytes = key.getBytes(Charsets.UTF_8);
            dataMap.put(keyBytes, data.get(key));
        }
        RedisFuture<Long> longRedisFuture = asyncRedisPool.hsetAsync(redisKey.getBytes(StandardCharsets.UTF_8), dataMap);
        return Homo.warp(homoSink -> {
            longRedisFuture.whenCompleteAsync((result, throwable) -> {
                try {
                    if (throwable != null) {
                        homoSink.error(throwable);
                        return;
                    }
                    //hset方法如果原来没这个field返回 1，如果存在，则是覆盖旧值，返回 0
                    homoSink.success(Boolean.TRUE);
                    log.trace("asyncUpdate end, appId_{} regionId_{} logicType_{} ownerId_{}, keys_{}", appId, regionId, logicType, ownerId, data.keySet());
                } catch (Exception e) {
                    homoSink.error(e);
                }
            });
        });
    }

    @Override
    public Homo<Boolean> asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data, long expireSeconds) {
        log.trace("asyncUpdate start, appId_{} regionId_{} logicType_{} ownerId_{} expireSeconds_{}", appId, regionId, logicType, ownerId, expireSeconds);
        if (expireSeconds <= 0) {
            //没有超时时间
            return asyncUpdate(appId, regionId, logicType, ownerId, data);
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
            return Homo.warp(homoSink -> {
                resultFlux.subscribe(
                        result -> {
                            try {
                                homoSink.success(Boolean.TRUE);
                                log.trace("asyncUpdate end, appId_{} regionId_{} logicType_{} ownerId_{} expireSeconds_{}", appId, regionId, logicType, ownerId, expireSeconds);
                            } catch (Exception e) {
                                homoSink.error(e);
                            }
                        },
                        homoSink::error
                );
            });
        }
    }

    @Override
    public Homo<Pair<Boolean, Map<String, Long>>> asyncIncr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData) {
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
        return Homo.warp(homoSink->{
            objectRedisFuture.whenCompleteAsync((result, throwable) -> {
                try {
                    if (throwable != null) {
                        homoSink.error(throwable);
                        return;
                    }
                    ArrayList resultList = (ArrayList) result;
                    Map<String, Long> resultMap = new HashMap<>(resultList.size() / 2);
                    for (int i = 0; i < resultList.size(); i += 2) {
                        resultMap.put((String) resultList.get(i), (Long) resultList.get(i + 1));
                    }
                    Pair<Boolean, Map<String, Long>> pair = new Pair<>(true, resultMap);
                    homoSink.success(pair);
                    log.info("asyncIncr end, appId_{} regionId_{} logicType_{} ownerId_{} incrKeys_{}", appId, regionId, logicType, ownerId, incrData.keySet());
                } catch (Exception e) {
                    homoSink.error(e);
                }
            });
        });
    }

    @Override
    public Homo<Boolean> asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys) {
        log.trace("asyncRemoveKeys start, appId_{} regionId_{} logicType_{} ownerId_{} keys_{}", appId, regionId, logicType, ownerId, remKeys);
        String redisKey = String.format(REDIS_KEY_TMPL, appId, regionId, logicType, ownerId);
        byte[][] keys = new byte[remKeys.size()][];
        int index = 0;
        for (String key : remKeys) {
            keys[index++] = key.getBytes(StandardCharsets.UTF_8);
        }
        RedisFuture<Long> longRedisFuture = asyncRedisPool.hdelAsync(redisKey.getBytes(StandardCharsets.UTF_8), keys);
        return Homo.warp(homoSink->{
            longRedisFuture.whenCompleteAsync((result,throwable)->{
                try {
                    if (throwable!=null){
                        homoSink.error(throwable);
                        return;
                    }
                    //返回值:被成功移除的域的数量，不包括被忽略的域。
                    homoSink.success(true);
                    log.trace("asyncRemoveKeys end, appId_{} regionId_{} logicType_{} ownerId_{} keys_{}", appId, regionId, logicType, ownerId, remKeys);
                }catch (Exception e){
                    homoSink.error(e);
                }
            });
        });
    }
}
