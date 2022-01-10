package com.homo.core.facade.rpc;

import com.homo.core.utils.callback.CallBack2;

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
    String getType();

    /**
     * 接受一个byte 远程调用
     * @param srcService 返送方服务名
     * @param funName 函数名
     * @param params 参数
     * @param callBack 支持多参数的回调
     */
    void onCall(String srcService, String funName, Object params, CallBack2<String, byte[][]> callBack) throws Exception;

    /**
     * 处理调用异常
     *
     * @param msgId
     * @param e
     * @return
     */
    byte[] processError(String msgId, Throwable e);
}
