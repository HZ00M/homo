package com.homo.core.utils.delegate;

import com.homo.core.utils.exception.HomoError;
import lombok.extern.log4j.Log4j2;

/**
 * 广播  方法签名（无参无回调）
 */
@Log4j2
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
