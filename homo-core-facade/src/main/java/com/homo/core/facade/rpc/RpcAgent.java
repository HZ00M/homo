package com.homo.core.facade.rpc;

import com.homo.core.utils.rector.Homo;

public interface RpcAgent<T extends RpcContent<P, R>, P, R> {
     Homo<R> rpcCall(String funName, T content);
}
