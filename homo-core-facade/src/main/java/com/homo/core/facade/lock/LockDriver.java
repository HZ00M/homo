package com.homo.core.facade.lock;

import com.homo.core.common.facade.Driver;
import com.homo.core.utils.callback.CallBack;

public interface LockDriver extends Driver {

    /**
     * 异步加锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     * @param lockVal     期望值
     * @param uniqueId    令牌
     * @param expireTime  锁过期时间
     * @param callBack         回调返回是否解锁成功
     */
    void asyncLock(String appId, String regionId, String logicType, String ownerId, String lockField, String lockVal, String uniqueId, Integer expireTime, CallBack<Boolean> callBack);

    /**
     * 异步解锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     * @param uniqueId    令牌
     * @param callBack         回调返回是否解锁成功
     */
    void asyncUnlock(String appId,String regionId,String logicType,String ownerId,
                     String lockField, String uniqueId, CallBack<Boolean> callBack);


    /**
     * 同步加锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     * @param lockVal     期望值
     * @param uniqueId    令牌
     * @param expireTime  锁过期时间
     * @return
     */
    boolean lock(String appId,String regionId,String logicType,String ownerId,
                 String lockField, String lockVal, String uniqueId, Integer expireTime);

    /**
     *
     * 同步解锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     * @param uniqueId    令牌
     * @return
     */
    boolean unlock(String appId,String regionId,String logicType,String ownerId,
                   String lockField, String uniqueId);
}
