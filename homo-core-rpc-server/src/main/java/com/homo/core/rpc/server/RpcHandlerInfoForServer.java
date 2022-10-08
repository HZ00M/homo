package com.homo.core.rpc.server;

import com.homo.core.rpc.base.serial.RpcHandleInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcHandlerInfoForServer extends RpcHandleInfo {
    public RpcHandlerInfoForServer(Class<?> rpcClazz) {
        exportMethodInfos(rpcClazz);
    }
}
