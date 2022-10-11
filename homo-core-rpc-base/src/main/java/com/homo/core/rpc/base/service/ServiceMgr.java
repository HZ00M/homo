package com.homo.core.rpc.base.service;

import com.homo.core.common.module.ServiceModule;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 负责管理本服务上的所有service
 */
public class ServiceMgr implements ServiceModule {
     @Autowired(required = false)
     private Set<Service> services = new HashSet<>();
     private Map<String,Service> localServiceMap = new HashMap<>();
     @Autowired(required = false)
     private ServiceStateMgr serviceStateMgr;
     @Autowired(required = false)
     private ServiceStateHandler serviceStateHandler;


    public void init() {
        services.forEach(service -> {
            ServiceExport serviceExport = service.getServiceExport();
            if (serviceExport != null) {
                if (serviceExport.isStateful()){
                    getServerInfo().setStateful(true);
                }
                if (serviceExport.isMainServer()){
                    getServerInfo().setServerName(serviceExport.tagName());
                }

            }
            if (service instanceof BaseService){
                ((BaseService)service).init(this, serviceStateHandler);
            }
            localServiceMap.put(service.getServiceName(),service);
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
