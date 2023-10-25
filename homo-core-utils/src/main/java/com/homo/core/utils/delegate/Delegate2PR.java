package com.homo.core.utils.delegate;

import lombok.extern.slf4j.Slf4j;

/**
 *  广播且带回调  函数签名（两个参数一个返回）
 */
@Slf4j
public class Delegate2PR<P1,P2,R> extends BroadCasterCall<Delegate2PR.ExecuteFun<P1,P2,R>>{
    @Override
    protected Object execute(ExecuteFun<P1,P2, R> target, Object... objects) throws Exception {
        return target.apply((P1) objects[0],(P2) objects[1]);
    }

    @FunctionalInterface
    public interface ExecuteFun<P1,P2,R>{
        R apply(P1 p1,P2 p2)throws Exception;
    }

    public R publish(P1 p1,P2 p2){
        return (R)call(p1,p2);
    }
}
