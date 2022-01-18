package com.homo.core.facade.lock;

import com.homo.core.common.facade.Driver;
import com.homo.core.utils.callback.CallBack;

;

public interface LockDriver<T> extends Driver {

    /**
     * 上锁
     * @param lockObj 加锁对象
     * @param expireTime 过期时间
     * @param callBack 回调结果
     */
    void asyncLock(T lockObj, Integer expireTime, CallBack<Boolean> callBack);

    /**
     * 解锁
     * @param lockObj 加锁对象
     * @param callBack 回调结果
     */
    void asyncUnlock(T lockObj, CallBack<Boolean> callBack);
}
