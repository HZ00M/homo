package com.homo.core.rpc.client;

import com.homo.core.common.module.ServiceModule;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.exception.HomoException;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.base.utils.ServiceUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class RpcClientMgr<T> implements ServiceModule, ApplicationContextAware {
    private Map<RpcType,RpcClientFactory<T>> rpcClientFactoryMap = new HashMap<>();
    @Autowired(required = false)
    private ServiceStateHandler serviceStateHandler;
    @Autowired(required = false)
    private ServiceMgr serviceMgr;
    private final Map<String, RpcAgentClient<T>> rpcAgentClientMap = new HashMap();
    private ApplicationContext applicationContext;

    @Override
    public void init() {
        Map<String, RpcClientFactory> rpcClientImplMap = applicationContext.getBeansOfType(RpcClientFactory.class);
        for (RpcClientFactory clientFactory : rpcClientImplMap.values()) {
            rpcClientFactoryMap.put(clientFactory.getType(),clientFactory);
        }
    }

    /**
     * 通过服务名获得 一种CallDriver实例
     * @param tagName 服务标记名 name+port
     * @param hostname 服务器域名
     * @return RpcClientDriver 调用驱动器
     */
    public RpcAgentClient<T> getRpcAgentClient(String tagName,String hostname,RpcType rpcType) throws HomoException {
       RpcAgentClient<T> rpcAgentClient ;
        if (!rpcClientFactoryMap.containsKey(rpcType)){
            log.error("RpcAgent no support rpcType {} tagName {}",rpcType,tagName);
            throw HomoError.throwError(HomoError.rpcAgentTypeNotSupport,rpcType,tagName);
        }
        synchronized (RpcAgentClient.class){
           //k8s域名格式: hostname(tagName带-n序号).tagName.namespaceId.svc.cluster.local
           rpcAgentClient = rpcAgentClientMap.computeIfAbsent(hostname,s -> {
               String host = ServiceUtil.getServiceHostName(hostname);
               int port = ServiceUtil.getServicePort(tagName);
               log.info(
                       "new agent begin, tagName_{} hostname_{} port_{}",
                       tagName,
                       hostname,
                       port);

               RpcAgentClient<T> newAgent = rpcClientFactoryMap.get(rpcType).newAgent(host, port,serviceMgr.getServiceExportInfo(tagName).isStateful());
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
