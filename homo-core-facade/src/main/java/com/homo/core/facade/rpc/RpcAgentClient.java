package com.homo.core.facade.rpc;

/**
 * Rpc客户端驱动
 */
public interface RpcAgentClient<T extends RpcContent<P, R>, P, R> extends RpcAgent<T, P, R> {

}
