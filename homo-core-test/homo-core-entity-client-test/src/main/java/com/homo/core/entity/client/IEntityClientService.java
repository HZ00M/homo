package com.homo.core.entity.client;

import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.test.TestEntityRequest;
import io.homo.proto.entity.test.TestEntityResponse;
import io.homo.proto.entity.test.UserLoginRequest;
import io.homo.proto.entity.test.UserLoginResponse;

@ServiceExport(tagName = "entity-client:30011",isMainServer = true,isStateful = true,driverType = RpcType.grpc)
@RpcHandler
public interface IEntityClientService {

    Homo<UserLoginResponse> login(Integer pod, ParameterMsg parameterMsg, UserLoginRequest request);

    Homo<String> remoteEntityCall(ParameterMsg parameterMsg);

    Homo<TestEntityResponse> remoteEntityCall(Integer pod, ParameterMsg parameterMsg, TestEntityRequest testEntityRequest);

    Homo<String> innerRpcCall(String param);

    Homo<String> innerCallAndRemoteCall(ParameterMsg parameterMsg);

    Homo<String> entityServiceCall(EntityRequest param) throws Exception;
}
