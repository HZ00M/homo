package com.homo.core.rpc.base.service;

import com.homo.core.common.module.Module;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.serial.RpcHandleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ServiceMgr implements Module{
     private Set<Service> services;
     private Map<String,Service> localServiceMap;
     @Autowired
     private ServiceStateMgr serviceStateMgr;
     @Autowired
     private ServiceStateHandler serviceStateHandler;
     @Autowired
     private RpcHandleInfo rpcHandleInfo;


    public ServiceMgr(Set<Service> services){
        this.services = services;
    }

    public void init() {
        services.forEach(service -> {
            localServiceMap.put(service.getServiceName(),service);
            ServiceExport serviceExport = service.getClass().getAnnotation(ServiceExport.class);
            if (serviceExport != null) {
                if (serviceExport.isStateful()){
                    getServerInfo().setStateful(true);
                }
            }
            if (service instanceof BaseService){
                CallDispatcher callDispatcher = new CallDispatcher(service.getServiceName(),service,rpcHandleInfo);
                ((BaseService)service).init(this, serviceStateHandler, rpcHandleInfo, callDispatcher);
            }
        });
    }

    public Set<Service> getServices() {
        return services;
    }

    public Service getService(String serviceName){
        return localServiceMap.get(serviceName);
    }

    public boolean isLocalService(String serviceName, Integer podIndex) {
        if (localServiceMap.containsKey(serviceName)){
            return serviceStateMgr==null ||podIndex.equals(serviceStateMgr.getPodIndex());
        }
        return false;
    }

    public ServiceStateMgr getStateMgr() {
        return serviceStateMgr;
    }

    public Service getMainService() {
        //todo
        return null;
    }
}
