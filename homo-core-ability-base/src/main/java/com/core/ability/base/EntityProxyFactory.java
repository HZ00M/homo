package com.core.ability.base;

import com.core.ability.base.call.CallSystem;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.rpc.client.RpcClientMgr;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class EntityProxyFactory {
    @Autowired
    CallSystem callSystem;
    @Autowired
    EntityService entityService;
    @Autowired
    RpcClientMgr rpcClientMgr;

    @SuppressWarnings("unchecked")
    public  <T> T getEntityProxy(String serviceName, Class<T> entityHandlerInterface, String id) throws Exception{
        RpcAgentClient agentClient = rpcClientMgr.getRpcAgentClient(entityService.getTagName(), entityService.getHostName(), entityService.getType());
        EntityRpcProxy entityRpcProxy = new EntityRpcProxy(agentClient, entityHandlerInterface,id,serviceName);
        return (T)entityRpcProxy.getProxyInstance();
    }
}
