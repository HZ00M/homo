package com.homo.core.rpc.base.config;

import com.homo.core.configurable.rpc.ServerStateProperties;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.base.state.ServiceStateHandlerImpl;
import com.homo.core.rpc.base.state.ServiceStateMgrImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Configuration
@Log4j2
@Import(ServerStateProperties.class)
public class RpcBaseAutoConfiguration {

    @Bean("serviceMgr")
    public ServiceMgr serviceMgr(){
        log.info("register bean serviceMgr");
        return new ServiceMgr();
    }

    @Bean("serviceStateHandler")
    public ServiceStateHandler serviceStateHandler(){
        log.info("register bean serviceStateHandler");
        return new ServiceStateHandlerImpl();
    }

    @DependsOn({"serviceStateHandler","serviceMgr","statefulDriver"})
    @Bean("serviceStateMgr")
    public ServiceStateMgr serviceStateMgr(){
        log.info("register bean serviceStateMgr");
        return new ServiceStateMgrImpl();
    }

}
