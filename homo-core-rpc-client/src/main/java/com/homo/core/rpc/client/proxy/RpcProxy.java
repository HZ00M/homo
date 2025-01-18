package com.homo.core.rpc.client.proxy;

import brave.Span;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceInfo;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.serial.MethodDispatchInfo;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.base.utils.ServiceUtil;
import com.homo.core.rpc.client.ExchangeHostName;
import com.homo.core.rpc.client.RpcClientMgr;
import com.homo.core.rpc.client.RpcHandlerInfoForClient;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.exception.HomoException;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoInterfaceUtil;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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
    private final String tagName;
    private final RpcType rpcType;
    private final RpcHandlerInfoForClient rpcHandlerInfoForClient;

    public RpcProxy(RpcClientMgr rpcClientMgr, Class<?> interfaceType, ServiceMgr serviceMgr, ServiceStateMgr serviceStateMgr) throws Exception {
        this.rpcClientMgr = rpcClientMgr;
        this.interfaceType = interfaceType;
        ServiceExport serviceExport = interfaceType.getAnnotation(ServiceExport.class);
        this.tagName = serviceExport.tagName();
        this.rpcType = serviceExport.driverType();
        this.serviceMgr = serviceMgr;
        this.serviceStateMgr = serviceStateMgr;
        this.rpcHandlerInfoForClient = new RpcHandlerInfoForClient(interfaceType);
        /**
         * 第一次调用前初始化
         * 第一次调用前相关的环境遍历应该已经初始化好了
         */
        ServiceExport export = interfaceType.getAnnotation(ServiceExport.class);
        if (export != null) {
            String tag = export.tagName();
            String serviceHostName = ServiceUtil.getServiceHostNameByTag(tag);
            Integer port = ServiceUtil.getServicePortByTag(tag);
            boolean stateful = export.isStateful() ;
            ServiceInfo serviceInfo = new ServiceInfo(tag, serviceHostName, port, stateful,rpcType.ordinal());
            serviceStateMgr.setLocalServiceInfo(tag, serviceInfo);
        }
    }

    @Override
    public Homo intercept(Object o, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        //接口及其继承的接口都找不到此方法,调用对象父类方法
        if (!interfaceType.equals(declaringClass) && !HomoInterfaceUtil.getAllInterfaces(interfaceType).contains(declaringClass)) {
            return (Homo) methodProxy.invokeSuper(o, params);
        }
        String methodName = method.getName();
        log.trace(
                "intercept service {}, method {} class {}",
                tagName,
                methodName,
                declaringClass.getSimpleName());
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span currentSpan = ZipkinUtil.getTracing().tracer().currentSpan();
        if (currentSpan == null){
            currentSpan = ZipkinUtil.getTracing().tracer().newTrace();
        }
        Span finalCurrentSpan = currentSpan;
        return GetBeanUtil.getBean(ServiceStateMgr.class).getServiceInfo(tagName)
                .nextDo(serviceInfo -> {
                    return ExchangeHostName.exchange(serviceInfo, params)
                            .nextDo(realHostName -> {
                                if (!StringUtils.isEmpty(realHostName)) {
                                    boolean isStateful = ServiceUtil.isStatefulService(realHostName);
                                    RpcContent rpcContent = rpcHandlerInfoForClient.serializeParamForInvokeRemoteMethod(methodName, params);
                                    rpcContent.setSpan(finalCurrentSpan);
                                    return rpcClientMgr
                                            .getAgentClient(realHostName, serviceInfo)
                                            .rpcCall(methodName, rpcContent)
                                            .switchThread(callQueue,finalCurrentSpan)
                                            .nextDo(ret -> {
                                                finalCurrentSpan.annotate(ZipkinUtil.CLIENT_RECEIVE_TAG).finish();
                                                return processReturn(methodName, rpcContent);
                                            })
                                            .catchError(throwable -> {
                                                finalCurrentSpan.error((Throwable) throwable);
                                                log.error("rpc client call throwable {}", throwable);
                                                HomoError.throwError(HomoError.rpcAgentTypeNotSupport);
                                            })
                                            ;
                                } else {
                                    finalCurrentSpan.error(new RuntimeException("realHostName empty"));
                                    return Homo.result(HomoError.throwError(HomoError.hostNotFound, tagName));
                                }
                            });
                });
    }

    @NotNull
    private Homo processReturn(String methodName, RpcContent rpcContent) throws HomoException {
        String msgId = rpcContent.getId();
        if (!msgId.equals(methodName)) {//返回的msgId不等于方法名  抛出异常
            return Homo.error(HomoError.throwError(HomoError.defaultError, msgId));
        }
        Object returnValue = rpcHandlerInfoForClient.unSerializeReturnValue(methodName, rpcContent);
        return Homo.result(returnValue);
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
