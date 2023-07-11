package com.homo.core.facade.rpc;

import com.homo.core.utils.rector.Homo;

public interface RpcAgent<T extends RpcContent> {
     Homo rpcCall(String funName, T params);
}
