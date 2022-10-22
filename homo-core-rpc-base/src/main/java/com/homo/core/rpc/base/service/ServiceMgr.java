package com.homo.core.rpc.base.service;

import com.homo.core.common.module.ServiceModule;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 负责管理本服务上的所有service
 */
@Log4j2
public class ServiceMgr implements ServiceModule {
     @Autowired(required = false)
     private Set<Service> services = new HashSet<>();
     private Map<String,Service> localServiceMap = new HashMap<>();
     @Autowired(required = false)
     @Lazy
     private ServiceStateMgr serviceStateMgr;
     @Autowired(required = false)
     private ServiceStateHandler serviceStateHandler;
     private Map<String,ServiceExport> serviceExportMap = new HashMap<>();
     private Service mainService;
    public void init() {
        scanServiceDefine();
        services.forEach(service -> {
            ServiceExport serviceExport = service.getServiceExport();
            if (serviceExport != null) {
                if (serviceExport.isStateful()){
                    getServerInfo().setStateful(true);
                }
                if (serviceExport.isMainServer()){
                    getServerInfo().setServerName(serviceExport.tagName());
                    mainService = service;
                }
            }
            if (service instanceof BaseService){
                log.info("service {} init start",service.getTagName());
                ((BaseService)service).init(this, serviceStateHandler);
                localServiceMap.put(service.getTagName(),service);
            }else {
                log.error("service must extend BaseService yet");
            }

        });
    }

    private void scanServiceDefine() {
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages("").addScanners(Scanners.TypesAnnotated));
        Set<Class<?>> exportClazz = reflections.getTypesAnnotatedWith(ServiceExport.class,true);
        for (Class<?> clazz : exportClazz) {
            ServiceExport serviceExport = clazz.getAnnotation(ServiceExport.class);
            serviceExportMap.put(serviceExport.tagName(),serviceExport);
        }
    }

    public Set<Service> getServices() {
        return services;
    }

    public Service getLocalService(String serviceName){
        return localServiceMap.get(serviceName);
    }

    public ServiceExport getServiceExportInfo(String serviceName){
        return serviceExportMap.get(serviceName);
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
        return mainService;
    }
}
