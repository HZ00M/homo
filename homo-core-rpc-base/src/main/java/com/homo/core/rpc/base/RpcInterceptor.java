package com.homo.core.rpc.base;

import com.homo.core.utils.rector.Homo;

public interface RpcInterceptor<RETURN> {
    Homo<RETURN> onCall(Object handle, String funName, Object[] params,CallData callData);

}
