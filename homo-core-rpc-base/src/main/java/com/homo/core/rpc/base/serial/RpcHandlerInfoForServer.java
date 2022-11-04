package com.homo.core.rpc.base.serial;

import com.homo.core.facade.serial.RpcContent;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RpcHandlerInfoForServer extends RpcHandleInfo {
    public RpcHandlerInfoForServer(Class<?> rpcClazz) {
        Class<?>[] interfaces = rpcClazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            exportMethodInfos(anInterface);
        }
    }

    public byte[][] serializeForReturn(String funName, Object[] param) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.serializeReturn(param);
    }

    public Object[] unSerializeParamForInvoke(String funName,RpcContent rpcContent) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.unSerializeParam(rpcContent);
    }
}
