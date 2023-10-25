package com.homo.core.utils.delegate;

import com.homo.core.utils.exception.HomoError;
import lombok.extern.slf4j.Slf4j;

/**
 * 广播  方法签名（2个参数无回调）
 */
@Slf4j
public class Delegate2PVoid<P1,P2> extends BroadCaster<Delegate2PVoid.ExecuteFun<P1,P2>> {
    @FunctionalInterface
    public interface ExecuteFun<P1,P2> {
        void run(P1 p1,P2 p2) throws Exception;
    }

    @Override
    boolean run(ExecuteFun<P1,P2> executeFun, Object... objects) throws Exception {
        executeFun.run((P1)objects[0],(P2)objects[1]);
        return true;
    }

    public void publish(P1 p1,P2 p2) {
        try {
            broadcast(p1,p2);
        } catch (Exception e) {
            log.error("broadcast e", e);
            throw HomoError.throwError(HomoError.broadcastError,"DelegatePubSub publish error");
        }
    }
}
