package com.homo.core.rpc.server.facade;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;
import reactor.util.function.Tuple2;

@ServiceExport(tagName = "rpc-server-stateless:30012",isMainServer = false,isStateful = false,driverType = RpcType.grpc)
@RpcHandler
public interface RpcServiceFacade {
    Homo<String> jsonCall(Integer podId, ParameterMsg parameterMsg,String jsonStr);

    Homo<Integer> objCall(Integer podId, ParameterMsg parameterMsg,TestObjParam testObjParam);

    Homo<TestServerResponse> pbCall(Integer podId, ParameterMsg parameterMsg,TestServerRequest request);

    Homo<Tuple2<String,Integer>> tupleCall(Integer podId, ParameterMsg parameterMsg);
}
