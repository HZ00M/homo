package com.homo.core.facade.rpc;

import com.homo.core.utils.rector.Homo;

public interface RpcInterceptor<RETURN> {
    Homo<RETURN> onCall(Object handle, String funName, Object[] params);
}
