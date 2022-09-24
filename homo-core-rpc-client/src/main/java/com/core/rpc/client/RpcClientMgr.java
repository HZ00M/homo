package com.core.rpc.client;

import com.homo.core.common.module.Module;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.rpc.base.cache.ServiceCache;
import com.homo.core.rpc.base.service.ServiceMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RpcClientMgr implements Module {
    @Autowired
    private RpcClientFactory rpcClientFactory;
    @Autowired
    private ServiceCache serviceCache;
    @Autowired
    private ServiceMgr serviceMgr;
    private Map<String, RpcAgentClient> rpcAgentClientMap = new HashMap();

    @Override
    public void init() {

    }
}
