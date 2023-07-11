package com.homo.core.facade.rpc;

import com.homo.core.utils.rector.Homo;
import io.homo.proto.rpc.JsonReq;
import io.homo.proto.rpc.Req;
import io.homo.proto.rpc.StreamReq;

public interface RpcClient {
    Homo asyncBytesCall(Req req);

    Homo asyncBytesStreamCall(String reqId,StreamReq streamReq);

    Homo asyncJsonCall(JsonReq jsonReq);
}
