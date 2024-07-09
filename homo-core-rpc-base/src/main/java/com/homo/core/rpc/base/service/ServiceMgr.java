package com.homo.core.rpc.base.service;

import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.utils.module.DriverModule;
import com.homo.core.utils.module.RootModule;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ServiceMgr implements DriverModule {
    @Autowired(required = false)
    @Lazy
    private Set<Service> services = new HashSet<>();
    private Map<String, Service> localServiceMap = new HashMap<>();
    @Autowired(required = false)
    @Lazy
    private ServiceStateMgr serviceStateMgr;
    private Map<String, ServiceExport> serviceExportMap = new HashMap<>();
    private Service mainService;
    @Autowired
    private RootModule rootModule;
    @Override
    public void moduleInit() {
        scanServiceDefine();
        for (Service service : services) {
            ServiceExport serviceExport = service.getServiceExport();
            if (serviceExport != null) {
                if (serviceExport.isStateful()) {
                    rootModule.getServerInfo().setStateful(true);
                }
                if (serviceExport.isMainServer()) {
                    mainService = service;
                }
            }
            if (service instanceof BaseService) {
                ((BaseService) service).init();
                log.info("service {} init start", service.getTagName());
                localServiceMap.put(service.getTagName(), service);
            } else {
                log.error("service must extend BaseService yet");
            }
        }
        if (mainService == null){
            log.error("main service not found");
            System.exit(-1);
        }
        rootModule.getServerInfo().setServerName(mainService.getTagName());
    }

    private void scanServiceDefine() {
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages("").addScanners(Scanners.TypesAnnotated));
        Set<Class<?>> exportClazz = reflections.getTypesAnnotatedWith(ServiceExport.class, true);
        for (Class<?> clazz : exportClazz) {
            ServiceExport serviceExport = clazz.getAnnotation(ServiceExport.class);
            serviceExportMap.put(serviceExport.tagName(), serviceExport);
        }
    }

    public Set<Service> getServices() {
        return services;
    }

    public Service getLocalService(String serviceName) {
        return localServiceMap.get(serviceName);
    }

    public ServiceExport getServiceExportInfo(String serviceName) {
        return serviceExportMap.get(serviceName);
    }

    public boolean isLocalService(String serviceName, Integer podIndex) {
        if (localServiceMap.containsKey(serviceName)) {
            return serviceStateMgr == null || podIndex.equals(serviceStateMgr.getPodIndex());
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
