package com.homo.core.maven;

import com.homo.core.facade.service.ServiceExport;
import io.kubernetes.client.common.KubernetesType;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Builder
@Data
@Slf4j
@ToString(exclude = "serviceExport")
public class HomoServiceSetter {
    private Class<?> serviceClass;
    private ServiceExport serviceExport;
    private String serviceName;
    private Integer servicePort;
    private boolean isStateful;
    private boolean isMain;
    public void init(){
        String tagName = serviceExport.tagName();
        String[] names = tagName.split(":");
        serviceName = names[0];
        servicePort = Integer.valueOf(names[1]);
        isStateful = serviceExport.isStateful();
        isMain = serviceExport.isMainServer();
    }

}
