package com.homo.core.rpc.base.serial;

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
        Method[] methods = rpcClazz.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            try {
                MethodDispatchInfo methodDispatchInfo = MethodDispatchInfo.create(method);
                methodDispatchInfoMap.put(methodName, methodDispatchInfo);
            } catch (Exception e) {
                log.error("exportMethodInfos msgName {} error {}", methodName, e);
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
