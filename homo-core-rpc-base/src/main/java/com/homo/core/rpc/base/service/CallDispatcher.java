package com.homo.core.rpc.base.service;

import com.homo.core.rpc.base.RpcInterceptor;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.rpc.base.CallData;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.rpc.base.serial.RpcHandlerInfoForServer;
import com.homo.core.utils.concurrent.queue.IdCallQueue;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.exception.HomoException;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CallDispatcher {
    public RpcHandlerInfoForServer rpcHandleInfo;
    protected RpcInterceptor interceptor;

    public Homo callFun(Object handler, String srcService, String funName, RpcContent rpcContent) throws HomoException {
        return callFun(handler, srcService, funName, rpcContent, null, null);
    }

    public CallDispatcher(RpcHandlerInfoForServer rpcHandleInfo, RpcInterceptor interceptor) {
        this.rpcHandleInfo = rpcHandleInfo;
        this.interceptor = interceptor;
    }

    public CallDispatcher(RpcHandlerInfoForServer rpcHandleInfo) {
        this(rpcHandleInfo, null);
    }

    public Homo callFun(Object handler, String srcService, String funName, RpcContent rpcContent, IdCallQueue callQueue, Integer queueId) {
        MethodDispatchInfo methodDispatchInfo = rpcHandleInfo.getMethodDispatchInfo(funName);
        if (!methodDispatchInfo.isCallAllowed(srcService)) {
            log.error("callFun srcService {} funName {} not allow", srcService, funName);
            throw HomoError.throwError(HomoError.callAllow);
        }
        Object[] unSerializeParam = rpcHandleInfo.unSerializeParamForInvoke(funName, rpcContent);
        Homo<CallData> callTask = Homo.result(new CallData(handler, methodDispatchInfo, unSerializeParam, queueId, srcService, callQueue));
        Homo retPromise;
        if (interceptor != null) {
            retPromise = callTask.nextDo(call -> interceptor.onCall(handler, funName, unSerializeParam, call));
        } else {
            retPromise = callTask.nextDo(call -> call.onCall(handler, funName, unSerializeParam, null));
        }
        return retPromise;
    }


    public void setInterceptor(RpcInterceptor interceptor) {
        if (this.interceptor != null) {
            log.error("CallDispatcher setInterceptor recall!");
        }
        this.interceptor = interceptor;
    }
}
