package com.homo.service.redis.lock.driver;

import com.homo.core.facade.lock.LockDriver;
import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.core.utils.callback.CallBack;
import io.lettuce.core.RedisFuture;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

;

@Log4j2
public class RedisLockDriver implements LockDriver {
    @Autowired(required = false)
    @Qualifier("homoRedisPool")
    private HomoAsyncRedisPool redisPool;

    private static final String REDIS_LOCK_TMPL = "slug:{%s:%s:%s:%s:lock}";

    @Override
    public void asyncLock(String appId, String regionId, String logicType, String ownerId, String lockField,Integer expireTime, CallBack<Boolean> callBack) {
        log.info("asyncLock start : {} {} {} {} {} ", appId, regionId, logicType, ownerId, lockField);
        String redisLockKey = String.format(REDIS_LOCK_TMPL, appId, regionId, logicType, ownerId);
        String lockScript = LuaScriptHelper.lockScript;

        String[] keyList = new String[1];
        String[] argList = new String[2];
        keyList[0] = redisLockKey;
        argList[0] = String.valueOf(expireTime);
        argList[1] = lockField;
        RedisFuture<Object> future = redisPool.evalAsync(lockScript, keyList, argList);
        future.whenComplete((result,throwable)-> {
            log.info("asyncLock redis completed result_{}",result,throwable);
        })
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
    public void asyncUnlock(String appId, String regionId, String logicType, String ownerId, String lockField, CallBack<Boolean> callBack) {
        log.info("asyncUnlock start : {} {} {} {} {} {}", appId, regionId, logicType, ownerId, lockField);
        String redisLockKey = String.format(REDIS_LOCK_TMPL, appId, regionId, logicType, ownerId);

        String unlockScript = LuaScriptHelper.unLockScript;
        String[] keyList = new String[1];
        keyList[0] = redisLockKey;
        String[] args = new String[1];
        args[0] = lockField;
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
    public boolean lock(String appId, String regionId, String logicType, String ownerId, String lockField, Integer expireTime) {
        log.info("lock start : {} {} {} {} {} ", appId, regionId, logicType, ownerId, lockField);
        String redisLockKey = String.format(REDIS_LOCK_TMPL, appId, regionId, logicType, ownerId);
        String lockScript = LuaScriptHelper.lockScript;

        String[] keyList = new String[1];
        String[] argList = new String[2];
        keyList[0] = redisLockKey;
        argList[0] = String.valueOf(expireTime);
        argList[1] = lockField;
        Object result = redisPool.eval(lockScript, keyList, argList);
        List<Object> resultList = (List<Object>) result;
        return (Long) resultList.get(0) == 1;
    }

    @Override
    public boolean unlock(String appId, String regionId, String logicType, String ownerId, String lockField) {
        log.info("unlock start : {} {} {} {} {} ", appId, regionId, logicType, ownerId, lockField);
        String redisLockKey = String.format(REDIS_LOCK_TMPL, appId, regionId, logicType, ownerId);

        String unlockScript = LuaScriptHelper.unLockScript;
        String[] keyList = new String[1];
        keyList[0] = redisLockKey;
        String[] args = new String[1];
        args[0] = lockField;
        Object result = redisPool.eval(unlockScript, keyList, args);
        List<Object> resultList = (List<Object>) result;
        return (Long) resultList.get(0) == 1;
    }
}
