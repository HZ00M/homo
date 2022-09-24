package com.core.rpc.client.proxy;

import com.core.rpc.client.RpcClientMgr;
import com.core.rpc.client.RpcHandlerInfoForClient;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.rpc.base.cache.ServiceCache;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class RpcProxy implements MethodInterceptor {
    private RpcClientMgr rpcClientMgr;
    private Class<?> interfaceType;
    private String serviceName;
    private ServiceCache serviceCache;
    private RpcHandlerInfoForClient rpcHandlerInfoForClient;
    private boolean inited = false;
    public RpcProxy(RpcClientMgr rpcClientMgr, Class<?> interfaceType, ServiceCache serviceCache) throws Exception {
        this.rpcClientMgr = rpcClientMgr;
        this.interfaceType = interfaceType;
        this.serviceName = interfaceType.getAnnotation(ServiceExport.class).ServiceName();
        this.serviceCache = serviceCache;
        this.rpcHandlerInfoForClient = new RpcHandlerInfoForClient(interfaceType);

    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        initWhenFirstCall();
        return null;
    }

    /**
     * 为目标对象生成代理对象 //todo 是否有必要
     *
     * @return 返回代理对象
     */
    public Object getProxyInstance() {
        // 工具类
        Enhancer en = new Enhancer();
        // 设置父类
        en.setSuperclass(interfaceType);
        // 设置回调函数
        en.setCallback(this);
        // 创建子类对象代理
        return en.create();
    }

    /**
     * 第一次调用前初始化
     * 第一次调用前相关的环境遍历应该已经初始化好了
     */
    private void initWhenFirstCall(){
        if (!inited){
            inited = true;
            // 如果已经制定服务名，就为服务名设置一个和自身相等的tag，这个tag只在本进程有效，全局的tag需要由服务端来设置
            ServiceExport export = interfaceType.getAnnotation(ServiceExport.class);
            if (export != null){
                String tag = export.ServiceName();
                serviceCache.setLocalServiceNameTag(tag, serviceName);
            }
        }
    }

}
