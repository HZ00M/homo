package com.homo.mock.client.service;

import com.core.ability.base.EntityProxyFactory;
import com.core.ability.base.StorageEntityMgr;
import com.homo.core.entity.facade.UserEntityFacade;
import com.homo.core.entity.facade.EntityServiceFacade;
import com.homo.core.facade.ability.IEntityService;
import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.utils.rector.Homo;
import com.homo.mock.client.entity.ClientEntity;
import com.homo.mock.client.entity.ClientEntityFacade;
import com.homo.mock.client.facade.ClientEntityServiceFacade;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.EntityResponse;
import io.homo.proto.entity.test.QueryInfoRequest;
import io.homo.proto.entity.test.UserLoginRequest;
import io.homo.proto.entity.test.UserLoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientEntityService extends BaseService implements ClientEntityServiceFacade {

    @Autowired
    EntityProxyFactory entityProxyFactory;
    @Autowired
    StorageEntityMgr entityMgr;

    @Override
    public Homo<UserLoginResponse> login(Integer pod, ParameterMsg parameterMsg, UserLoginRequest request) {
        String userId = parameterMsg.getUserId();
        return entityMgr.getOrCreateEntityPromise(ClientEntity.class, userId)
                .nextValue(clientEntity -> {
                    log.info("login userId {} clientEntity {}", userId, clientEntity);
                    return UserLoginResponse.newBuilder().setCode(0).setMsg("success").build();
                });
    }


    @Override
    public Homo<Long> queryRemoteServerInfo(Integer pod, ParameterMsg parameterMsg) {
        QueryInfoRequest request = QueryInfoRequest.newBuilder().setChannelId(parameterMsg.getChannelId()).build();
        UserEntityFacade serverEntity = entityProxyFactory.getEntityProxy(EntityServiceFacade.class, UserEntityFacade.class, parameterMsg.getUserId());
        return serverEntity.queryInfo(request)
                .nextValue(ret -> {
                    long queryTime = ret.getBeforeQueryTime();
                    log.info("queryRemoteServerInfo queryTime {}", queryTime);
                    return queryTime;
                });
    }

    @Override
    public Homo<String> innerRpcCall(Integer pod, ParameterMsg parameterMsg) {
        String userId = parameterMsg.getUserId();
        log.info("innerRpcCall call userId {}", userId);
        ClientEntityFacade clientEntity = entityProxyFactory.getEntityProxy(ClientEntityServiceFacade.class, ClientEntityFacade.class, userId);
        return clientEntity.clientCall("clientCall param")
                .nextValue(ret -> {
                    log.info("clientCall ret {}", ret);
                    return ret;
                });
    }

    @Override
    public Homo<String> innerCallAndRemoteCall(ParameterMsg parameterMsg) {
        log.info("innerCallAndRemoteCall call {}", parameterMsg);
        String userId = parameterMsg.getUserId();
        ClientEntityFacade clientEntity = entityProxyFactory.getEntityProxy(ClientEntityServiceFacade.class, ClientEntityFacade.class, userId);
        UserEntityFacade entityProxy = entityProxyFactory.getShareProxy(IEntityService.class, UserEntityFacade.class, userId);
        QueryInfoRequest request = QueryInfoRequest.newBuilder().setChannelId(parameterMsg.getChannelId()).build();
        return clientEntity.clientCall("innerCallAndRemoteCall param")
                .nextDo(ret -> {
                    log.info("clientCall ret {}", ret);
                    return entityProxy.queryInfo(request)
                            .nextValue(res -> {
                                int code = res.getCode();
                                log.info("queryInfo ret {}", code);
                                return code + "";
                            });
                });
    }

}
