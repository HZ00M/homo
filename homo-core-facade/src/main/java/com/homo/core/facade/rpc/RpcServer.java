package com.homo.core.facade.rpc;

import com.homo.core.utils.rector.Homo;

/**
 * Rpc服务器接口
 * 驱动层使用，接收rpc调用，分发到本服务器具体函数
 */
public interface RpcServer {

    /**
     * 获取rpc服务器的域名
     *
     * @return rpc服务器域名
     */
    String getHostName();

    /**
     * 获取rpc端口
     *
     * @return rpc服务器端口
     */
    int getPort();

    /**
     * 服务驱动的类型，如grpc或http
     * @return
     */
    RpcType getType();

    /**
     * 接受一个byte 远程调用
     * @param srcService 返送方服务名
     * @param funName 函数名
     * @param param 参数
     */
     <T,R> Homo<R> onCall(String srcService, String funName, RpcContent<T,R> param) ;

}
