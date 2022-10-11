package com.homo.core.rpc.base.serial;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcHandlerInfoForServer extends RpcHandleInfo {
    public RpcHandlerInfoForServer(Class<?> rpcClazz) {
        Class<?>[] interfaces = rpcClazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            exportMethodInfos(anInterface);
        }
    }
}
