package com.homo.core.rpc.server;

import com.homo.core.facade.rpc.CallData;
import com.homo.core.facade.rpc.ServerRpcInterceptor;
import reactor.util.function.Tuple2;

import java.util.function.BiFunction;

public class CallDispatcher {
    static BiFunction<String, Throwable, byte[]> errorFun = null;
    protected ServerRpcInterceptor<CallData, Tuple2<String,Object[]>> interceptor;
}
