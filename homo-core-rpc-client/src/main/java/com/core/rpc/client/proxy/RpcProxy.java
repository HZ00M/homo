package com.core.rpc.client.proxy;

import com.core.rpc.client.RpcClientMgr;
import com.core.rpc.client.RpcHandlerInfoForClient;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.StateMgr;
import com.homo.core.rpc.base.cache.ServiceCache;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoInterfaceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@Slf4j
public class RpcProxy implements MethodInterceptor {
    private final StateMgr stateMgr;
    private final ServiceMgr serviceMgr;
    private final RpcClientMgr rpcClientMgr;
    private final Class<?> interfaceType;
    private final String serviceName;
    private final ServiceCache serviceCache;
    private final RpcHandlerInfoForClient rpcHandlerInfoForClient;
    public RpcProxy(RpcClientMgr rpcClientMgr, Class<?> interfaceType, ServiceCache serviceCache, ServiceMgr serviceMgr, StateMgr stateMgr) throws Exception {
        this.rpcClientMgr = rpcClientMgr;
        this.interfaceType = interfaceType;
        this.serviceName = interfaceType.getAnnotation(ServiceExport.class).tagName();
        this.serviceCache = serviceCache;
        this.serviceMgr = serviceMgr;
        this.stateMgr = stateMgr;
        this.rpcHandlerInfoForClient = new RpcHandlerInfoForClient(interfaceType);
        /**
         * 第一次调用前初始化
         * 第一次调用前相关的环境遍历应该已经初始化好了
         */
        ServiceExport export = interfaceType.getAnnotation(ServiceExport.class);
        if (export != null){
            String tag = export.tagName();
            serviceCache.setLocalServiceNameTag(tag, serviceName);
        }
    }

    @Override
    public Homo intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        //接口及其继承的接口都找不到此方法,调用对象父类方法
        if (!interfaceType.equals(declaringClass) && !HomoInterfaceUtil.getAllInterfaces(interfaceType).contains(declaringClass)) {
            return (Homo) methodProxy.invokeSuper(o, objects);
        }
            String methodName = method.getName();
        log.trace(
                "intercept service_{}, method_{} class_{}",
                serviceName,
                methodName,
                declaringClass.getSimpleName());
        MethodDispatchInfo methodDispatchInfo = rpcHandlerInfoForClient.getMethodDispatchInfo(methodName);
        RpcContent rpcContent = methodDispatchInfo.serializeParam(objects);
        String hostname = choiceServiceHostName(o,method,objects);//todo 区分有状态调用和无状态调用
        return rpcClientMgr
                .getRpcAgentClient(serviceName,hostname)
                .rpcCall(methodName,rpcContent);
    }

    private String choiceServiceHostName(Object o, Method method, Object[] objects) {
        String hostName = serviceName;
        Service service = serviceMgr.getService(serviceName);
        if (service.isStateful()){
            //todo 获取指定服务地址
        }
        return hostName;
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


}
