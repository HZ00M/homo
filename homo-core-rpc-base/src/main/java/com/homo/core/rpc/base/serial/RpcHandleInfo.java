package com.homo.core.rpc.base.serial;

import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
@Log4j2
public class RpcHandleInfo {
    private Map<String, MethodDispatchInfo> methodDispatchInfoMap = new HashMap<>();


    public void exportMethodInfos(Class<?> rpcClazz) {
        if (!rpcClazz.isInterface()){
            return;
        }
        Class<?> annotationInterface = HomoAnnotationUtil.findAnnotationInterface(rpcClazz, ServiceExport.class);
        if (annotationInterface == null){
            return;
        }
        Method[] methods = annotationInterface.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            try {
                MethodDispatchInfo methodDispatchInfo = MethodDispatchInfo.create(method);
                log.info("exportMethodInfos rpcClazz {}  msgName {} ",rpcClazz,methodName);
                methodDispatchInfoMap.put(methodName, methodDispatchInfo);
            } catch (Exception e) {
                log.error("exportMethodInfos rpcClazz {} msgName {} error {}",rpcClazz, methodName, e);
            }
        }
        Class<?>[] subRpcClasses = rpcClazz.getInterfaces();
        for (Class<?> subRpcClass : subRpcClasses) {
            exportMethodInfos(subRpcClass);
        }
    }

    public MethodDispatchInfo getMethodDispatchInfo(String funName) {
        return methodDispatchInfoMap.get(funName);
    }
}
