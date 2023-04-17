package com.homo.core.rpc.base.service;

import com.homo.core.facade.rpc.RpcInterceptor;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.rpc.base.CallData;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.rpc.base.serial.RpcHandlerInfoForServer;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.exception.HomoException;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CallDispatcher {

    private final String srcName;
    Object handler;

    RpcHandlerInfoForServer rpcHandleInfo;

    protected RpcInterceptor interceptor;

    public CallDispatcher(String name, Object handler, RpcHandlerInfoForServer rpcHandleInfo) {
        this.srcName = name;
        this.handler = handler;
        this.rpcHandleInfo = rpcHandleInfo;
    }

    public Homo callFun(String srcService, String funName, RpcContent rpcContent) throws HomoException {
        MethodDispatchInfo methodDispatchInfo = rpcHandleInfo.getMethodDispatchInfo(funName);
        if (!methodDispatchInfo.isCallAllowed(srcService)) {
            log.error("callFun srcService {} funName {} not allow", srcService, funName);
            throw HomoError.throwError(HomoError.callAllow);
        }
        Object[] unSerializeParam = rpcHandleInfo.unSerializeParamForInvoke(funName,rpcContent);
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
