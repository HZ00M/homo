package com.homo.core.entity.client;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.test.UserLoginRequest;
import io.homo.proto.entity.test.UserLoginResponse;

@ServiceExport(tagName = "entity-client:30012",isMainServer = true,isStateful = true,driverType = RpcType.grpc)
@RpcHandler
public interface IEntityClientService {

    Homo<UserLoginResponse> login(Integer pod, ParameterMsg parameterMsg, UserLoginRequest request);

    Homo<String> localEntityCall(ParameterMsg parameterMsg);

    Homo<String> innerRpcCall(String param);

    Homo<String> innerCallAndRemoteCall(ParameterMsg parameterMsg);

    Homo<String> entityServiceCall(EntityRequest param) throws Exception;
}
