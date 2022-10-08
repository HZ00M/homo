package com.homo.core.rpc.base.service;

import com.homo.core.facade.rpc.RpcInterceptor;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.rpc.base.CallData;
import com.homo.core.rpc.base.exception.CallAllowException;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.rpc.base.serial.RpcHandleInfo;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallDispatcher {

    private final String srcName;
    Object handler;

    RpcHandleInfo rpcHandleInfo;

    protected RpcInterceptor interceptor;

    public CallDispatcher(String name, Object handler, RpcHandleInfo rpcHandleInfo) {
        this.srcName = name;
        this.handler = handler;
        this.rpcHandleInfo = rpcHandleInfo;
    }

    public Homo callFun(String srcService, String funName, RpcContent rpcContent) throws CallAllowException {
        MethodDispatchInfo methodDispatchInfo = rpcHandleInfo.getMethodDispatchInfo(funName);
        if (!methodDispatchInfo.isCallAllowed(srcService)) {
            log.error("callFun srcService {} funName {} not allow", srcService, funName);
            throw new CallAllowException("call fun not allow");
        }
        Object[] unSerializeParam = methodDispatchInfo.unSerializeParam(rpcContent);
        RpcInterceptor callTask;
        if (interceptor != null) {
            callTask = interceptor;
        } else {
            callTask = new CallData(handler, methodDispatchInfo, unSerializeParam, null, srcName);
        }
        return callTask.onCall(handler, funName, unSerializeParam);
    }

    public void setInterceptor(RpcInterceptor interceptor) {
        if (this.interceptor != null) {
            log.error("CallDispatcher setInterceptor recall!");
        }
        this.interceptor = interceptor;
    }
}
