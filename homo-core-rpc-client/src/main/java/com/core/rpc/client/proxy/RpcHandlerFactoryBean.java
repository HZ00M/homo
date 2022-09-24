package com.core.rpc.client.proxy;

import com.core.rpc.client.RpcClientMgr;
import com.homo.core.rpc.base.cache.ServiceCache;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

@Slf4j
public class RpcHandlerFactoryBean<T> extends AbstractFactoryBean<T> {
    private final Class<T> interfaceType;
    @Autowired
    private RpcClientMgr rpcClientMgr;
    @Autowired
    private ServiceCache serviceCache;

    public RpcHandlerFactoryBean(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceType;
    }

    @Override
    protected @NotNull T createInstance() throws Exception {
        log.info("get RpcProxy from  RpcHandlerFactoryBean, interfaceType_{}", interfaceType.getName());
        return RpcProxyMgr.createProxy(rpcClientMgr,interfaceType, serviceCache);
    }
}
