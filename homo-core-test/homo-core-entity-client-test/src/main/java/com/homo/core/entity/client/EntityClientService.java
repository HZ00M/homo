package com.homo.core.entity.client;

import com.core.ability.base.EntityProxyFactory;
import com.core.ability.base.StorageEntityMgr;
import com.homo.core.entity.facade.IServerEntity;
import com.homo.core.entity.facade.IServerEntityService;
import com.homo.core.facade.ability.IEntityService;
import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.EntityResponse;
import io.homo.proto.entity.test.TestEntityRequest;
import io.homo.proto.entity.test.TestEntityResponse;
import io.homo.proto.entity.test.UserLoginRequest;
import io.homo.proto.entity.test.UserLoginResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class EntityClientService extends BaseService implements IEntityClientService {

    @Autowired
    EntityProxyFactory entityProxyFactory;
    @Autowired
    StorageEntityMgr entityMgr;

    @Override
    public Homo<UserLoginResponse> login(Integer pod, ParameterMsg parameterMsg, UserLoginRequest request) {
        String userId = parameterMsg.getUserId();
        return entityMgr.getOrCreateEntityPromise(ClientTestTestEntity.class, userId)
                .nextValue(user -> {
                    log.info("login userId {} user {}", userId, user);
                    return UserLoginResponse.newBuilder().setCode(0).setMsg("success").build();
                });
    }

    @Override
    public Homo<String> remoteEntityCall(ParameterMsg parameterMsg) {
        log.info("localEntityCall call {}", parameterMsg);
        IClientTestEntity clientEntity = entityProxyFactory.getEntityProxy(IEntityClientService.class, IClientTestEntity.class, parameterMsg.getUserId());
        return clientEntity.clientCall("success");
    }

    @Override
    public Homo<TestEntityResponse> remoteEntityCall(Integer pod, ParameterMsg parameterMsg, TestEntityRequest testEntityRequest) {
        IServerEntity serverEntity = entityProxyFactory.getEntityProxy(IServerEntityService.class, IServerEntity.class, parameterMsg.getUserId());
        return serverEntity.login(testEntityRequest);
    }

    @Override
    public Homo<String> innerRpcCall(String param) {
        log.info("innerRpcCall call {}", param);
        IServerEntity entityProxy = entityProxyFactory.getEntityProxy(IEntityService.class, IServerEntity.class, "123");
        return entityProxy.login(TestEntityRequest.newBuilder().setParam("123").build())
                .nextValue(ret -> {
                    int code = ret.getCode();
                    log.info("innerRpcCall code {}", code);
                    return code + "";
                });
    }

    @Override
    public Homo<String> innerCallAndRemoteCall(ParameterMsg parameterMsg) {
        log.info("innerCallAndRemoteCall call {}", parameterMsg);
        String userId = parameterMsg.getUserId();
        IClientTestEntity clientEntity = entityProxyFactory.getEntityProxy(IEntityClientService.class, IClientTestEntity.class, userId);
        IServerEntity entityProxy = entityProxyFactory.getShareProxy(IEntityService.class, IServerEntity.class, userId);
        return clientEntity.clientCall(userId)
                .nextDo(ret -> {
                    log.info("innerCallAndRemoteCall clientCall {}", ret);
                    return entityProxy.login(TestEntityRequest.newBuilder().setParam(userId).build())
                            .nextValue(res -> {
                                int code = res.getCode();
                                log.info("innerCallAndRemoteCall login {}", code);
                                return code + "";
                            });
                });
    }

    @Override
    public Homo<String> entityServiceCall(EntityRequest param) throws Exception {
        EntityRequest entityRequest = EntityRequest.newBuilder().build();
        TestEntityRequest testEntityRequest = TestEntityRequest.newBuilder().setParam("123").build();
        return entityCall(0, ParameterMsg.newBuilder().build(),entityRequest)
                .nextDo(ret -> {
                    EntityResponse response = ret;
                    log.info("entityServiceCall response {}", response);
                    return Homo.result("success");
                });
    }
}
