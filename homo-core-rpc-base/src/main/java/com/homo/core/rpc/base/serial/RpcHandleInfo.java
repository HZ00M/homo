package com.homo.core.rpc.base.serial;

import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.ability.IEntityService;
import com.homo.core.facade.service.InnerService;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
@Log4j2
public class RpcHandleInfo {
    private Map<String, MethodDispatchInfo> methodDispatchInfoMap = new HashMap<>();


    public void exportMethodInfos(Class<?> rpcClazz) {
        if (!rpcClazz.isInterface()){
            //只暴露接口方法
            return;
        }
        Map<Class<?>, Annotation> annotations = HomoAnnotationUtil.findAnnotations(rpcClazz);
        if (!annotations.containsKey(ServiceExport.class) &&
                !annotations.containsKey(InnerService.class) &&
                !annotations.containsKey(EntityType.class)){
            //只暴露指定注解的接口
            return;
        }
        Method[] methods = rpcClazz.getMethods();
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
