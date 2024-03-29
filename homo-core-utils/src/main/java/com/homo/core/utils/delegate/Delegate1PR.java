package com.homo.core.utils.delegate;

import lombok.extern.slf4j.Slf4j;

/**
 *  广播且带回调   函数签名（一个参数一个返回）
 */
@Slf4j
public class Delegate1PR<P,R> extends BroadCasterCall<Delegate1PR.ExecuteFun<P,R>>{
    @Override
    protected Object execute(ExecuteFun<P, R> target, Object... objects) throws Exception {
        return target.apply((P) objects[0]);
    }

    @FunctionalInterface
    public interface ExecuteFun<P,R>{
        R apply(P p)throws Exception;
    }

    public R publish(P p){
        return (R)call(p);
    }
}
