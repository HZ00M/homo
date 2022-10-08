package com.core.rpc.client;

import com.homo.core.facade.serial.RpcContent;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.rpc.base.serial.RpcHandleInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcHandlerInfoForClient extends RpcHandleInfo {
    public RpcHandlerInfoForClient(Class<?> rpcClazz) {
        exportMethodInfos(rpcClazz);
    }

    public RpcContent serializeParamForInvoke(String funName, Object[] param) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.serializeParam(param);
    }

    public Object[] unSerializeParamForCallback(String funName,RpcContent rpcContent) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.unSerializeParam(rpcContent);
    }

}
