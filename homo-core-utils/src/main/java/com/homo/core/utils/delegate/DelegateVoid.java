package com.homo.core.utils.delegate;

import com.homo.core.utils.exception.HomoError;
import lombok.extern.slf4j.Slf4j;

/**
 * 广播  方法签名（无参无回调）
 */
@Slf4j
public class DelegateVoid extends BroadCaster<DelegateVoid.ExecuteFun> {
    @FunctionalInterface
    public interface ExecuteFun {
        void run() throws Exception;
    }

    @Override
    boolean run(ExecuteFun executeFun, Object... objects) throws Exception {
        executeFun.run();
        return true;
    }

    public void publish() {
        try {
            broadcast();
        } catch (Exception e) {
            log.error("broadcast e", e);
            throw HomoError.throwError(HomoError.broadcastError,"DelegatePubSub publish error");
        }
    }
}
