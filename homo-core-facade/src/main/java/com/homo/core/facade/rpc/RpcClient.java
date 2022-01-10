package com.homo.core.facade.rpc;

import com.homo.core.utils.callback.CallBack2;

/**
 * Rpc客户端驱动
 */
public interface RpcClient {
    /**
     * Rpc 接口
     *
     * @param funName   函数名
     * @param params    参数数组
     * @param callBack2 回调函数，返回字节流
     */
    void rpcCall(String funName, Object params, CallBack2<String, byte[][]> callBack2);
}
