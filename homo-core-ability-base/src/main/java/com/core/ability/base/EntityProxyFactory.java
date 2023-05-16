package com.core.ability.base;

import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.base.utils.ServiceUtil;
import com.homo.core.rpc.client.RpcClientMgr;
import com.homo.core.utils.spring.GetBeanUtil;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程代理工厂
 */
@Log4j2
public class EntityProxyFactory {

    ThreadLocal<Map<Class<?>,EntityRpcProxy>> entityType2ProxyMapThreadLocal = new ThreadLocal<>();
    ThreadLocal<Map<Class<?>, Object>> proxyThreadLocal = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    public <T> T getEntityProxy(Class<?> serviceZz, Class<T> entityHandler, String id) {
        return getEntityProxy(ServiceUtil.getServiceTagName(serviceZz), entityHandler, id);
    }

    public  <T> T getEntityProxy(String serverHostName, Class<T> entityHandlerInterface, String id) {
        ServiceMgr serviceMgr = GetBeanUtil.getBean(ServiceMgr.class);
        RpcClientMgr rpcClientMgr = GetBeanUtil.getBean(RpcClientMgr.class);
        EntityType entityType = entityHandlerInterface.getAnnotation(EntityType.class);
//        RpcAgentClient agentClient = rpcClientMgr.getRpcAgentClient(entityType.type(), serverHostName, serviceMgr.getMainService().getType());
        EntityRpcProxy entityRpcProxy = new EntityRpcProxy(rpcClientMgr,entityHandlerInterface,id, serverHostName);
        return (T)entityRpcProxy.getProxyInstance();
    }


    public <T> T getShareProxy(Class<T> entityType, String id){
        return getShareProxy("", entityType, id);
    }

    public <T> T getShareProxy(Class<?> serviceZz, Class<T> entityType, String id){
        return getShareProxy(ServiceUtil.getServiceTagName(serviceZz), entityType, id);
    }
    /**
     * 返回type共享的proxy, 只是修改了id而已。注意：该方法获取的proxy内部不能再进行rpc调用，否则会有多线程安全问题
     * @param serviceName 服务名
     * @param entityHandlerInterface entity类型
     * @param id id
     * @return proxy
     * @param <T> 类型参数
     */

    public <T> T getShareProxy(String serviceName, Class<T> entityHandlerInterface, String id) {
        Map<Class<?>, EntityRpcProxy> entityRpcProxyMap = entityType2ProxyMapThreadLocal.get();
        if (entityRpcProxyMap == null) {
            entityType2ProxyMapThreadLocal.set(new ConcurrentHashMap<>());
            proxyThreadLocal.set(new ConcurrentHashMap<>());
        }
        Map<Class<?>, Object> proxyInstanceMap = proxyThreadLocal.get();
        RpcClientMgr rpcClientMgr = GetBeanUtil.getBean(RpcClientMgr.class);
        ServiceMgr serviceMgr = GetBeanUtil.getBean(ServiceMgr.class);
        entityRpcProxyMap.computeIfAbsent(entityHandlerInterface,clazz->{
            EntityRpcProxy entityRpcProxy = new EntityRpcProxy(rpcClientMgr, entityHandlerInterface,id, serviceName);
            proxyInstanceMap.put(entityHandlerInterface,entityRpcProxy.getProxyInstance());
            return entityRpcProxy;
        }).setId(id);
        return (T)proxyInstanceMap.get(entityHandlerInterface);
    }
}
