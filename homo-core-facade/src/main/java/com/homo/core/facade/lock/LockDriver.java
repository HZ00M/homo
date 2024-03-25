package com.homo.core.facade.lock;

import com.homo.core.utils.Driver;
import com.homo.core.utils.rector.Homo;

public interface LockDriver extends Driver {

    /**
     * 异步加锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     * @param expireTime  锁过期时间
     */
    Homo<Boolean> asyncLock(String appId, String regionId, String logicType, String ownerId, String lockField, Integer expireTime);

    /**
     * 异步解锁
     * @param appId       appId
     * @param regionId    regionId
     * @param logicType   逻辑类型
     * @param ownerId     ID
     * @param lockField   指定key
     */
    Homo<Boolean> asyncUnlock(String appId,String regionId,String logicType,String ownerId,
                     String lockField);


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
