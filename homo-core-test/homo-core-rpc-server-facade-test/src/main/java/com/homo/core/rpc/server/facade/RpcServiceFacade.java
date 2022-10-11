package com.homo.core.rpc.server.facade;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;
import reactor.util.function.Tuple2;

@ServiceExport(tagName = "rpc-server:30011",isMainServer = true,isStateful = true)
@RpcHandler
public interface RpcServiceFacade {
    Homo<String> jsonCall(String jsonStr);

    Homo<Integer> objCall(TestObjParam testObjParam);

    Homo<TestServerResponse> pbCall(TestServerRequest request);

    Homo<TestServerResponse> targetCall(Integer podId, ParameterMsg parameterMsg, TestServerRequest request);

    Homo<Tuple2<String,Integer>> tupleCall();
}
