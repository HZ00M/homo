package com.homo.core.rpc.client.facade;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;

@ServiceExport(tagName = "rpc-client:30010",isMainServer = true,isStateful = true)
@RpcHandler
public interface RpcClientFacade {

    Homo<Integer> objCall(Integer param);
}
