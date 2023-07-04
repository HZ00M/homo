package com.homo.core.facade.rpc;

import com.homo.core.utils.rector.Homo;

public interface RpcAgent {
     Homo rpcCall(String funName, RpcContent params);
}
