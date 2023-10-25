package com.homo.core.utils.delegate;

import com.homo.core.utils.exception.HomoError;
import lombok.extern.slf4j.Slf4j;

/**
 * 广播  方法签名（1个参数无回调）
 */
@Slf4j
public class Delegate1PVoid<P1> extends BroadCaster<Delegate1PVoid.ExecuteFun<P1>> {
    @FunctionalInterface
    public interface ExecuteFun<P1> {
        void run(P1 p1) throws Exception;
    }

    @Override
    boolean run(ExecuteFun<P1> executeFun, Object... objects) throws Exception {
        executeFun.run((P1)objects[0]);
        return true;
    }

    public void publish(P1 p1) {
        try {
            broadcast(p1);
        } catch (Exception e) {
            log.error("broadcast e", e);
            throw HomoError.throwError(HomoError.broadcastError,"DelegatePubSub publish error");
        }
    }
}
