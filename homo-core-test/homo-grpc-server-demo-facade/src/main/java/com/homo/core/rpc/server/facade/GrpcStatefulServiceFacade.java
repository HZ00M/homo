package com.homo.core.rpc.server.facade;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.rpc.server.vo.ParamVO;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;
import reactor.util.function.Tuple2;

/**
 * 声明一个grpc有状态服务
 */
@ServiceExport(tagName = "grpc-server-stateful:30303",isMainServer = true,isStateful = true,driverType = RpcType.grpc)
@RpcHandler
public interface GrpcStatefulServiceFacade {
    Homo<JSONObject> jsonCall(Integer podId, ParameterMsg parameterMsg, JSONObject jsonStr);

    Homo<Integer> objCall(Integer podId, ParameterMsg parameterMsg, ParamVO paramVO);

    Homo<TestServerResponse> pbCall(Integer podId, ParameterMsg parameterMsg,TestServerRequest request);

    Homo<Tuple2<String,Integer>> tupleCall(Integer podId, ParameterMsg parameterMsg);
}
