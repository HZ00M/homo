package com.homo.core.rpc.client.proxy;

import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.client.ExportHostName;
import com.homo.core.rpc.client.RpcClientMgr;
import com.homo.core.rpc.client.RpcHandlerInfoForClient;
import com.homo.core.utils.fun.MultiFunA;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoInterfaceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@Slf4j
public class RpcProxy implements MethodInterceptor {
    private final ServiceStateMgr serviceStateMgr;
    private final ServiceMgr serviceMgr;
    private final RpcClientMgr rpcClientMgr;
    private final Class<?> interfaceType;
    private final String serviceName;
    private final ServiceStateHandler serviceStateHandler;
    private final RpcHandlerInfoForClient rpcHandlerInfoForClient;
    private MultiFunA<String,Homo<String>> choiceHostFun = ExportHostName.STRATEGY0;
    public RpcProxy(RpcClientMgr rpcClientMgr, Class<?> interfaceType, ServiceStateHandler serviceStateHandler, ServiceMgr serviceMgr, ServiceStateMgr serviceStateMgr) throws Exception {
        this.rpcClientMgr = rpcClientMgr;
        this.interfaceType = interfaceType;
        this.serviceName = interfaceType.getAnnotation(ServiceExport.class).tagName();
        this.serviceStateHandler = serviceStateHandler;
        this.serviceMgr = serviceMgr;
        this.serviceStateMgr = serviceStateMgr;
        this.rpcHandlerInfoForClient = new RpcHandlerInfoForClient(interfaceType);
        /**
         * 第一次调用前初始化
         * 第一次调用前相关的环境遍历应该已经初始化好了
         */
        ServiceExport export = interfaceType.getAnnotation(ServiceExport.class);
        if (export != null){
            String tag = export.tagName();
            serviceStateHandler.setLocalServiceNameTag(tag, serviceName);
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
        return choiceHostFun.apply(serviceName,objects)
                .nextDo(hostName->{
                    if (!StringUtils.isEmpty(hostName)){
                        MethodDispatchInfo methodDispatchInfo = rpcHandlerInfoForClient.getMethodDispatchInfo(methodName);
                        RpcContent callContent = methodDispatchInfo.serializeParam(objects);
                        return rpcClientMgr
                                .getRpcAgentClient(serviceName,hostName)
                                .rpcCall(methodName, callContent)
                                .nextDo(ret->{
                                    RpcContent returnContent = (RpcContent) ret;
                                    Object[] param = rpcHandlerInfoForClient.unSerializeParamForCallback(methodName, returnContent);
                                    //todo 待完善
                                    return Homo.result(param);
                                })
                                ;
                    }else {
                        return Homo.error(new RuntimeException("can't find hostname"));
                    }
                });

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
