package com.homo.core.facade.rpc;

import com.homo.core.utils.rector.Homo;
import io.homo.proto.rpc.JsonReq;
import io.homo.proto.rpc.Req;
import io.homo.proto.rpc.StreamReq;

public interface RpcClient {
    <RETURN> Homo<RETURN> asyncBytesCall(Req req);

    <RETURN> Homo<RETURN> asyncBytesStreamCall(String reqId,StreamReq streamReq);

    <RETURN> Homo<RETURN> asyncJsonCall(JsonReq jsonReq);
}
