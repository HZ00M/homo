package com.homo.core.facade.rpc;

import com.homo.core.facade.serial.RpcContent;
import com.homo.core.utils.rector.Homo;

public interface RpcAgent {
    <RETURN, PARAM> Homo<RETURN> rpcCall(String funName, RpcContent<PARAM> params);
}
