package com.homo.service.lock.driver;

import com.homo.core.facade.lock.LockDriver;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.utils.callback.CallBack;
import com.homo.service.lock.lua.LuaScriptHelper;
import io.lettuce.core.RedisFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class RedisLockDriver implements LockDriver {
    @Autowired
    private LuaScriptHelper luaScriptHelper;
    private HomoAsyncRedisPool redisPool;

    private static final String REDIS_LOCK_TMPL = "lock:{%s:%s:%s:%s}:%s";

    public void init(HomoAsyncRedisPool redisPool){
        this.redisPool = redisPool;
    }

    @Override
    public void asyncLock(String appId, String regionId, String logicType, String ownerId, String lockField, String lockVal, String uniqueId, Integer expireTime, CallBack<Boolean> callBack) {
        log.info("asyncLock start : {} {} {} {} {} {}", appId, regionId, logicType, ownerId, lockField, uniqueId);
        String redisLockKey = String.format(REDIS_LOCK_TMPL, appId, regionId, logicType, ownerId, lockField);
        String lockScript = luaScriptHelper.getLockScript();

        String[] keyList = new String[1];
        String[] argList = new String[2];
        keyList[0] = redisLockKey;
        argList[0] = String.valueOf(expireTime);
        argList[1] = uniqueId;
        RedisFuture<Object> future = redisPool.evalAsync(lockScript, keyList, argList);
        future.whenComplete((result,throwable)-> log.info("asyncLock redis completed"))
                .thenRunAsync(()->{
                    try {
                        Object result = future.get();
                        log.info("asyncLock complete : {} {} {} {}", appId, regionId, logicType, ownerId);
                        List<Object> resultList = (List<Object>) result;
                        callBack.onBack((Long) resultList.get(0) == 1);
                        log.debug("result is {} ", result);
                    }catch (Exception e){
                        log.error("asyncLock exception {} {} {} {}", appId, regionId, logicType, ownerId, e);
                        callBack.onError(e);
                    }
                });
    }

    @Override
    public void asyncUnlock(String appId, String regionId, String logicType, String ownerId, String lockField, String uniqueId, CallBack<Boolean> callBack) {
        log.info("asyncUnlock start : {} {} {} {} {} {}", appId, regionId, logicType, ownerId, lockField, uniqueId);
        String redisLockKey = String.format(REDIS_LOCK_TMPL, appId, regionId, logicType, ownerId, lockField);

        String unlockScript = luaScriptHelper.getUnLockScript();
        String[] keyList = new String[1];
        keyList[0] = redisLockKey;
        String[] args = new String[1];
        args[0] = uniqueId;
        RedisFuture<Object> future = redisPool.evalAsync(unlockScript, keyList, args);
        future.whenComplete((result,throwable)-> log.info("asyncLock redis completed"))
                .thenRunAsync(()->{
                    try {
                        Object result = future.get();
                        log.info("asyncUnlock complete : {} {} {} {}", appId, regionId, logicType, ownerId);
                        List<Object> resultList = (List<Object>) result;
                        callBack.onBack((Long) resultList.get(0) == 1);
                        log.debug("result is {} ", result);
                    }catch (Exception e){
                        log.error("asyncUnlock exception {} {} {} {}", appId, regionId, logicType, ownerId, e);
                        callBack.onError(e);
                    }
                });
    }

    @Override
    public boolean lock(String appId, String regionId, String logicType, String ownerId, String lockField, String lockVal, String uniqueId, Integer expireTime) {
        log.info("lock start : {} {} {} {} {} {}", appId, regionId, logicType, ownerId, lockField, uniqueId);
        String redisLockKey = String.format(REDIS_LOCK_TMPL, appId, regionId, logicType, ownerId, lockField);
        String lockScript = luaScriptHelper.getLockScript();

        String[] keyList = new String[1];
        String[] argList = new String[2];
        keyList[0] = redisLockKey;
        argList[0] = String.valueOf(expireTime);
        argList[1] = uniqueId;
        Object result = redisPool.eval(lockScript, keyList, argList);
        List<Object> resultList = (List<Object>) result;
        return (Long) resultList.get(0) == 1;
    }

    @Override
    public boolean unlock(String appId, String regionId, String logicType, String ownerId, String lockField, String uniqueId) {
        log.info("unlock start : {} {} {} {} {} {}", appId, regionId, logicType, ownerId, lockField, uniqueId);
        String redisLockKey = String.format(REDIS_LOCK_TMPL, appId, regionId, logicType, ownerId, lockField);

        String unlockScript = luaScriptHelper.getUnLockScript();
        String[] keyList = new String[1];
        keyList[0] = redisLockKey;
        String[] args = new String[1];
        args[0] = uniqueId;
        Object result = redisPool.eval(unlockScript, keyList, args);
        List<Object> resultList = (List<Object>) result;
        return (Long) resultList.get(0) == 1;
    }
}
