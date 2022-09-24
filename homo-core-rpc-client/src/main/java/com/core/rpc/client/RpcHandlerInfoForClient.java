package com.core.rpc.client;

import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.serial.RpcContentType;
import com.homo.core.facade.serial.RpcHandleInfo;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import lombok.extern.slf4j.Slf4j;
import reactor.util.function.Tuple2;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RpcHandlerInfoForClient implements RpcHandleInfo<RpcContent, Object[]> {
    private Map<String, MethodDispatchInfo> methodDispatchInfoMap = new HashMap<>();

    public RpcHandlerInfoForClient(Class<?> rpcClazz) {
        exportMethodInfos(rpcClazz);
    }

    private void exportMethodInfos(Class<?> rpcClazz) {
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

    @Override
    public RpcContent unSerializeParamForInvoke(String funName, RpcContent rpcContent, Integer pod, Object parameterMsg) {
        return null;
    }

    @Override
    public Object[] serializeParamForReturn(RpcContentType contentType, String funKey, Tuple2<String, Object[]> result) {
        return new Object[0];
    }

}
