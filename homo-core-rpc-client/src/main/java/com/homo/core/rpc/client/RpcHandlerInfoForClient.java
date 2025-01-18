package com.homo.core.rpc.client;

import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.SerializeInfo;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.rpc.base.serial.RpcHandleInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcHandlerInfoForClient extends RpcHandleInfo {
    public RpcHandlerInfoForClient(Class<?> rpcClazz) {
        exportMethodInfos(rpcClazz);
    }

    public RpcContent serializeParamForInvokeRemoteMethod(String funName, Object[] param) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.warpToRpcContent(funName,param);
    }


    public Object serializeParamForCallback(String funName, RpcContent rpcContent) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.serializeForReturn(rpcContent);
    }

    public Object unSerializeReturnValue(String funName, RpcContent rpcContent) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        SerializeInfo returnSerializeInfo = methodDispatchInfo.getReturnSerializeInfo();
        return rpcContent.unSerializeReturnValue(returnSerializeInfo);

    }

}
