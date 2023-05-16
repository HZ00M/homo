package com.homo.core.rpc.base.serial;

import com.homo.core.facade.rpc.RpcContent;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RpcHandlerInfoForServer extends RpcHandleInfo {
    public RpcHandlerInfoForServer(Class<?> rpcClazz) {
        exportInterfaceMethod(rpcClazz);
    }

    public void exportInterfaceMethod(Class<?> rpcClazz){
        if (rpcClazz.isInterface()){
            exportMethodInfos(rpcClazz);
        }
        Class<?>[] interfaces = rpcClazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            exportMethodInfos(anInterface);
        }
        Class<?> superclass = rpcClazz.getSuperclass();
        if (superclass != null){
            exportInterfaceMethod(superclass);
        }
    }

    public byte[][] serializeForReturn(String funName, Object[] param) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.serializeReturn(param);
    }

    public Object[] unSerializeParamForInvoke(String funName, RpcContent rpcContent) {
        MethodDispatchInfo methodDispatchInfo = getMethodDispatchInfo(funName);
        return methodDispatchInfo.unSerializeParam(rpcContent);
    }
}
