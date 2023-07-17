package com.homo.core.facade.lock;

import com.homo.core.facade.Driver;
import com.homo.core.utils.callback.CallBack;

public interface LockDriver extends Driver {

    /**
     * 异步加锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     * @param expireTime  锁过期时间
     * @param callBack         回调返回是否解锁成功
     */
    void asyncLock(String appId, String regionId, String logicType, String ownerId, String lockField, Integer expireTime, CallBack<Boolean> callBack);

    /**
     * 异步解锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     * @param callBack         回调返回是否解锁成功
     */
    void asyncUnlock(String appId,String regionId,String logicType,String ownerId,
                     String lockField,  CallBack<Boolean> callBack);


    /**
     * 同步加锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     * @param expireTime  锁过期时间
     * @return
     */
    boolean lock(String appId,String regionId,String logicType,String ownerId,
                 String lockField, Integer expireTime);

    /**
     *
     * 同步解锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     * @return
     */
    boolean unlock(String appId,String regionId,String logicType,String ownerId,
                   String lockField);
}
