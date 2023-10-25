package com.homo.core.rpc.client.proxy;

import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.client.RpcClientMgr;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RpcProxyMgr {
    private RpcProxyMgr(){}

    static Map<Class<?>,Object> proxyMap = new HashMap<>();

    public static <T> T createProxy(RpcClientMgr rpcClientMgr, Class<T> interfaceType, ServiceMgr serviceMgr, ServiceStateMgr serviceStateMgr)throws Exception {
        Object proxy = proxyMap.get(interfaceType);
        if (proxy == null){
            proxy = new RpcProxy(rpcClientMgr,interfaceType, serviceMgr, serviceStateMgr).getProxyInstance();
            proxyMap.put(interfaceType,proxy);
        }
        @SuppressWarnings("unchecked")
        T obj = (T) proxy;
        return obj;
    }
}
