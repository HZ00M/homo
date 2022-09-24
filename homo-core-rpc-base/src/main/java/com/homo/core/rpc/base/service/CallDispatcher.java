package com.homo.core.rpc.base.service;

import com.homo.core.facade.rpc.RpcInterceptor;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcHandleInfo;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiFunction;

@Slf4j
public class CallDispatcher {

    RpcHandleInfo rpcHandleInfo;

    protected RpcInterceptor interceptor;

    public CallDispatcher(String name, RpcHandleInfo rpcHandleInfo){
        this.rpcHandleInfo = rpcHandleInfo;
    }

    public Homo callFun(String srcService, String funName, RpcContent param) {
        return null;
    }
    public void setInterceptor(RpcInterceptor interceptor) {
        if (this.interceptor != null) {
            log.error("CallDispatcher setInterceptor recall!");
        }
        this.interceptor = interceptor;
    }
}
