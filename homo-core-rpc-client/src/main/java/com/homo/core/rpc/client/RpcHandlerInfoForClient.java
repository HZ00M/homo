package com.homo.core.rpc.client;

import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.rpc.base.serial.RpcHandleInfo;
import io.homo.proto.client.ParameterMsg;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RpcHandlerInfoForClient extends RpcHandleInfo {
    public RpcHandlerInfoForClient(Class<?> rpcClazz) {
        exportMethodInfos(rpcClazz);
    }

    public RpcContent serializeParamForInvoke(String funName, Object[] param) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.serializeParamContent(param);
    }


    public Object[] unSerializeParamForCallback(String funName, RpcContent rpcContent) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.unSerializeReturn(rpcContent);
    }

}
