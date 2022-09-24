package com.homo.core.rpc.base.service;

import com.homo.core.common.module.Module;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.StateMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ServiceMgr implements Module{
     private Set<Service> services;
     @Autowired
     private StateMgr stateMgr;


    public ServiceMgr(Set<Service> services){
        this.services = services;
    }

    public void init() {
        services.forEach(service -> {
            ServiceExport serviceExport = service.getClass().getAnnotation(ServiceExport.class);
            if (serviceExport != null) {
                if (serviceExport.isStateful()){
                    getServerInfo().setStateful(true);
                }
            }
            if (service instanceof BaseService){
                ((BaseService)service).init(this, null, null, null);//todo 实现接口
            }
        });
    }

    public Set<Service> getServices() {
        return services;
    }

    public boolean isLocalService(String serviceName, Integer podIndex) {
        //todo
        return false;
    }

    public StateMgr getStateMgr() {
        return stateMgr;
    }

    public Service getMainService() {
        //todo
        return null;
    }
}
