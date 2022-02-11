package com.homo.core.root.storage;

import com.homo.core.common.exception.LockException;
import com.homo.core.common.module.Module;
import com.homo.core.facade.cache.CacheDriver;
import com.homo.core.facade.lock.LockDriver;
import com.homo.core.utils.callback.CallBack;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.MonoSink;

import java.util.List;
import java.util.Map;

/**
 * 响应式 实体存储模块
 */
@Slf4j
@Component
public class RectorEntityStorage<F, S, U, P> implements Module {
    private boolean useCache;   //todo 增加缓存支持
    @Autowired(required = false)
    EntityStorage<F, S, U, P> storage;
    @Autowired(required = false)
    LockDriver lockDriver;
    @Autowired(required = false)
    CacheDriver cacheDriver;
    public <T> void setUseCache(boolean isUse) {
        useCache = isUse;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public <T> Homo<List<T>> query(F filter,
                                   S sort,
                                   Integer limit,
                                   Integer skip,
                                   Class<T> clazz) {
        return Homo.warp(sink->{
            storage.asyncQuery(filter, sort, limit, skip, clazz, new CallBack<List<T>>() {
                @Override
                public void onBack(List<T> values) {
                    sink.success(values);
                }

                @Override
                public void onError(Throwable throwable) {
                    sink.error(throwable);
                }
            });
        });
    }

    public <T> Homo<Boolean> findAndModify(String logicType,
                                                 String ownerId,
                                                 String key,
                                                 F filter,
                                                 U update,
                                                 Class<T> clazz) {
        return Homo.warp((monoSink) -> storage.asyncFindAndModify(logicType,ownerId,key,filter, update, clazz, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean bool) {
                monoSink.success(bool);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T, V> Homo<List<V>> aggregate(P pipeline,
                                                Class<V> viewClazz,
                                                Class<T> clazz) {
        return Homo.warp((HomoSink<List<V>> monoSink) -> storage.asyncAggregate(pipeline, viewClazz, clazz, new CallBack<List<V>>() {
            @Override
            public void onBack(List<V> result) {
                monoSink.success(result);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }


    public <T> Homo<Pair<Boolean, Map<String, T>>> update(String logicType, String ownerId, Class<T> clazz, Map<String, T> keyList) {
        return Homo.warp(monoSink -> storage.updateWithCallBack(getServerInfo().getAppId(),getServerInfo().getRegionId(),logicType, ownerId, keyList, clazz, new CallBack<Pair<Boolean, Map<String, T>>>() {
            @Override
            public void onBack(Pair<Boolean, Map<String, T>> booleanMapMap) {
                monoSink.success(booleanMapMap);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<Pair<Boolean, Map<String, T>>> update(String appId, String regionId, String logicType, String ownerId, Class<T> clazz, Map<String, T> keyList) {
        return Homo.warp(monoSink -> storage.updateWithCallBack(appId, regionId, logicType, ownerId, keyList, clazz, new CallBack<Pair<Boolean, Map<String, T>>>() {
            @Override
            public void onBack(Pair<Boolean, Map<String, T>> booleanMapMap) {
                monoSink.success(booleanMapMap);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<Boolean> updatePartial(String logicType, String ownerId, String key, Class<T> clazz, Map<String, ?> keyList) {
        return Homo.warp(monoSink -> storage.updatePartialWithCallBack(getServerInfo().getAppId(),getServerInfo().getRegionId(),logicType, ownerId, key, keyList, clazz, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean booleanMapMap) {
                monoSink.success(booleanMapMap);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<Boolean> updatePartial(String appId, String regionId, String logicType, String ownerId, String key, Class<T> clazz, Map<String, ?> keyList) {
        return Homo.warp(monoSink -> storage.updatePartialWithCallBack(appId, regionId, logicType, ownerId, key, keyList, clazz, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean booleanMapMap) {
                monoSink.success(booleanMapMap);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<T> save(String logicType, String ownerId, String key, T data, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.save(getServerInfo().getAppId(),getServerInfo().getRegionId(),logicType, ownerId, key, data, clazz, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean aBoolean) {
                if (aBoolean) {
                    monoSink.success(data);
                } else {
                    monoSink.error(new Exception(String.format("save error, logicType_%d, ownerId_%s, key_%s", logicType, ownerId, key)));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<T> save(String appId, String regionId, String logicType, String ownerId, String key, T data, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.save(appId, regionId, logicType, ownerId, key, data, clazz, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean aBoolean) {
                if (aBoolean) {
                    monoSink.success(data);
                } else {
                    monoSink.error(new Exception(String.format("save error, appId_%s, regionId_%s logicType_%d, ownerId_%s, key_%s", appId, regionId, logicType, ownerId, key)));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<T> get(String logicType, String ownerId, String key, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.get(logicType, ownerId, key, clazz, new CallBack<T>() {
            @Override
            public void onBack(T bytes) {
                monoSink.success(bytes);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<T> get(String appId, String regionId, String logicType, String ownerId, String key, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.get(appId, regionId, logicType, ownerId, key, clazz, new CallBack<T>() {
            @Override
            public void onBack(T bytes) {
                monoSink.success(bytes);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<Map<String, T>> get(String logicType, String ownerId, List<String> keyList, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.get(logicType, ownerId, keyList, clazz, new CallBack<Map<String, T>>() {
            @Override
            public void onBack(Map<String, T> stringMap) {
                monoSink.success(stringMap);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<Map<String, T>> get(String appId, String regionId, String logicType, String ownerId, List<String> keyList, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.get(appId, regionId, logicType, ownerId, keyList, clazz, new CallBack<Map<String, T>>() {
            @Override
            public void onBack(Map<String, T> stringMap) {
                monoSink.success(stringMap);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<Map<String, T>> getAllKeysAndVal(String logicType, String ownerId, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.getAllKeysAndVal(logicType, ownerId, clazz, new CallBack<Map<String, T>>() {
            @Override
            public void onBack(Map<String, T> stringMap) {
                monoSink.success(stringMap);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<Map<String, T>> getAllKeysAndVal(String appId, String regionId, String logicType, String ownerId, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.getAllKeysAndVal(appId, regionId, logicType, ownerId, clazz, new CallBack<Map<String, T>>() {
            @Override
            public void onBack(Map<String, T> stringMap) {
                monoSink.success(stringMap);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<List<String>> removeKeys(String logicType, String ownerId, List<String> keys, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.removeKeys(logicType, ownerId, keys, clazz, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean aBoolean) {
                if (aBoolean) {
                    monoSink.success(keys);
                } else {
                    monoSink.error(new Exception(String.format("removeKeys failed, logicType_%d, ownerId_%s, keys_%s", logicType, ownerId, keys)));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<List<String>> removeKeys(String appId, String regionId, String logicType, String ownerId, List<String> keys, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.removeKeys(appId, regionId, logicType, ownerId, keys, clazz, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean aBoolean) {
                if (aBoolean) {
                    monoSink.success(keys);
                } else {
                    monoSink.error(new Exception(String.format("removeKeys failed, appId_%s regionId_%s logicType_%d, ownerId_%s, keys_%s", appId, regionId, logicType, ownerId, keys)));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public <T> Homo<Map<String, Long>> incr(String appId, String regionId, String logicType, String ownerId, String key, Map<String, Long> incrData, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.incr(appId, regionId, logicType, ownerId, key, incrData, clazz, new CallBack<Pair<Boolean, Map<String, Long>>>() {
            @Override
            public void onBack(Pair<Boolean, Map<String, Long>> booleanMapPair) {
                if (booleanMapPair.getLeft()) {
                    monoSink.success(booleanMapPair.getRight());
                } else {
                    monoSink.error(new Exception(String.format("incr failed, appId_%s regionId_%s logicType_%d, ownerId_%s, incrData_%s", appId, regionId, logicType, ownerId, incrData)));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(new Exception(String.format("incr error, appId_%s regionId_%s logicType_%d, ownerId_%s, incrData_%s", appId, regionId, logicType, ownerId, incrData)));
            }
        }));
    }

    public <T> Homo<Long> incr(String logicType, String ownerId, String incrKey, Class<T> clazz) {
        return Homo.warp(monoSink -> storage.incr(getServerInfo().getAppId(), getServerInfo().getRegionId(), logicType, ownerId, incrKey, clazz, new CallBack<Pair<Boolean, Long>>() {
            @Override
            public void onBack(Pair<Boolean, Long> booleanLongPair) {
                if (booleanLongPair.getLeft()) {
                    monoSink.success(booleanLongPair.getRight());
                } else {
                    monoSink.error(new Exception(String.format("incr failed, appId_%s regionId_%s logicType_%d, ownerId_%s, incrKey_%s", getServerInfo().getAppId(), getServerInfo().getRegionId(), logicType, ownerId, incrKey)));
                }
            }


            @Override
            public void onError(Throwable throwable) {
                monoSink.error(new Exception(String.format("incr error, appId_%s regionId_%s logicType_%d, ownerId_%s, incrKey_%s", getServerInfo().getAppId(), getServerInfo().getRegionId(), logicType, ownerId, incrKey)));
            }
        }));
    }

    public Homo<Boolean> asyncLock(String logicType, String ownerId,
                                         String lockField, String lockVal, String uniqueId, Integer expireTime) {
        return Homo.warp(monoSink -> storage.asyncLock(getServerInfo().getAppId(), getServerInfo().getRegionId(), logicType, ownerId, lockField, lockVal, uniqueId, expireTime, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean aBoolean) {
                monoSink.success(aBoolean);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }

    public Homo<Boolean> asyncLock(String logicType, String ownerId,
                                         String lockField, String lockVal, String uniqueId, Integer expireTime, Integer retryCount, Integer retryDelaySecond) {
        return Homo.warp((HomoSink<Boolean> monoSink) -> storage.asyncLock(getServerInfo().getAppId(), getServerInfo().getRegionId(), logicType, ownerId, lockField, lockVal, uniqueId, expireTime, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean aBoolean) {
                if (aBoolean) {
                    monoSink.success(aBoolean);
                } else {
                    monoSink.error(new LockException());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        })).retry(retryCount, retryDelaySecond, LockException.class);
    }

    public Homo<Boolean> asyncUnlock(String logicType, String ownerId,
                                           String lockField, String uniqueId) {
        return Homo.warp(monoSink -> storage.asyncUnlock(getServerInfo().getAppId(), getServerInfo().getRegionId(), logicType, ownerId, lockField, uniqueId, new CallBack<Boolean>() {
            @Override
            public void onBack(Boolean aBoolean) {
                monoSink.success(aBoolean);
            }

            @Override
            public void onError(Throwable throwable) {
                monoSink.error(throwable);
            }
        }));
    }


}
