package com.homo.mock.client.facade;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;

@ServiceExport(tagName = "mock-client:30301",isMainServer = true,isStateful = true)
@RpcHandler
public interface MockServiceFacade {

    Homo<Integer> mock(Integer param);
}
