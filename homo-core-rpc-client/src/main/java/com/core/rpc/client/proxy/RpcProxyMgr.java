package com.core.rpc.client.proxy;

import com.core.rpc.client.RpcClientMgr;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.service.ServiceMgr;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RpcProxyMgr {
    private RpcProxyMgr(){}

    static Map<Class<?>,Object> proxyMap = new HashMap<>();

    public static <T> T createProxy(RpcClientMgr rpcClientMgr, Class<T> interfaceType, ServiceStateHandler serviceStateHandler, ServiceMgr serviceMgr, ServiceStateMgr serviceStateMgr)throws Exception {
        Object proxy = proxyMap.get(interfaceType);
        if (proxy == null){
            proxy = new RpcProxy(rpcClientMgr,interfaceType, serviceStateHandler,serviceMgr, serviceStateMgr).getProxyInstance();
            proxyMap.put(interfaceType,proxy);
        }
        @SuppressWarnings("unchecked")
        T obj = (T) proxy;
        return obj;
    }
}
