package com.homo.core.facade.rpc;

import com.homo.core.common.facade.Driver;
import com.homo.core.utils.callback.CallBack2;

;

/**
 * rpc驱动层
 */
public interface RpcDriver extends Driver {
    /**
     * 创建一个Rpc客户端实例
     *
     * @param hostname 域名
     * @param port     端口
     * @return Rpc客户端实例
     */
    RpcClient createRpcClient(String hostname, int port);

    /**
     * 启动一个Rpc服务器
     *
     * @param rpcServer
     */
    void startServer(RpcServer rpcServer);

    /**
     * 异步rpc调用接口
     *
     * @param funName  函数名
     * @param param    参数数组
     * @param callBack 返回回调接口，返回字节流
     */
    void asyncCall(String funName, Object param, CallBack2<String, byte[][]> callBack);
}
