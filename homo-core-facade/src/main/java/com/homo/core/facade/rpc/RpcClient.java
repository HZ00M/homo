package com.homo.core.facade.rpc;

import com.homo.core.utils.rector.Homo;

/**
 * Rpc客户端驱动
 */
public interface RpcClient {
    /**
     * Rpc 接口
     *
     * @param funName   函数名
     * @param params    参数数组
     */
    <RETURN> Homo<RETURN> rpcCall(String funName, Object params);
}
