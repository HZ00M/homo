package com.homo.core.facade.rpc;

import com.homo.core.utils.module.DriverModule;


public interface RpcServerFactory extends DriverModule {
    /**
     * 驱动的类型，比如grpc,http等
     * @return 驱动类型字符串
     */
    RpcType getType();

    /**
     * 启动一个Rpc服务器
     * @param rpcServer
     */
    void startServer(RpcServer rpcServer);
}
