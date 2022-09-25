package com.core.rpc.client;

import com.homo.core.common.module.Module;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.service.Service;
import com.homo.core.rpc.base.ServiceUtil;
import com.homo.core.rpc.base.cache.ServiceCache;
import com.homo.core.rpc.base.service.ServiceMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
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

    /**
     * 通过服务名获得 一种CallDriver实例
     * @param tagName 服务标记名 name+port
     * @param hostname 服务器域名
     * @return RpcClientDriver 调用驱动器
     */
    public RpcAgentClient getRpcAgentClient(String tagName,String hostname) {
       RpcAgentClient rpcAgentClient ;
        Service service = serviceMgr.getService(tagName);
        synchronized (RpcAgentClient.class){
           //k8s域名格式: hostname(tagName带-n序号).tagName.namespaceId.svc.cluster.local
           rpcAgentClient = rpcAgentClientMap.computeIfAbsent(hostname,s -> {
               int port = ServiceUtil.getServicePort(tagName);
               log.info(
                       "new agent begin, tagName_{} hostname_{} port_{}",
                       tagName,
                       hostname,
                       port);
               RpcAgentClient newAgent = rpcClientFactory.newAgent(hostname, port,service.isStateful());
               log.info(
                       "new agent finish, tagName_{} hostname_{} port_{}",
                       tagName,
                       hostname,
                       port);
               return newAgent;
           });
       }
       return rpcAgentClient;
    }


}
