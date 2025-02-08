package com.homo.mock.client.facade;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.test.UserLoginRequest;
import io.homo.proto.entity.test.UserLoginResponse;

@ServiceExport(tagName = "client-entity-demo:30011",isMainServer = false,isStateful = true,driverType = RpcType.grpc)
@RpcHandler
public interface ClientEntityServiceFacade {

    Homo<UserLoginResponse> login(Integer pod, ParameterMsg parameterMsg, UserLoginRequest request);

    Homo<Long> queryRemoteServerInfo(Integer pod, ParameterMsg parameterMsg);

    Homo<String> innerRpcCall(Integer pod, ParameterMsg parameterMsg);

    Homo<String> innerCallAndRemoteCall(ParameterMsg parameterMsg);

}
