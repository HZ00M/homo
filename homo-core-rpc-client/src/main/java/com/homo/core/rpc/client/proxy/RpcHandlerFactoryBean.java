package com.homo.core.rpc.client.proxy;

import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.client.RpcClientMgr;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

@Log4j2
public class RpcHandlerFactoryBean<T> extends AbstractFactoryBean<T> {
    private final Class<T> interfaceType;
    @Autowired
    private RpcClientMgr rpcClientMgr;
    @Autowired
    private ServiceStateHandler serviceStateHandler;
    @Autowired
    private ServiceMgr serviceMgr;
    @Autowired
    private ServiceStateMgr serviceStateMgr;

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
        return RpcProxyMgr.createProxy(rpcClientMgr,interfaceType, serviceStateHandler,serviceMgr, serviceStateMgr);
    }
}
