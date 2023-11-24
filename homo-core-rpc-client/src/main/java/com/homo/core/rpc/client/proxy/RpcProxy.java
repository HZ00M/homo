package com.homo.core.rpc.client.proxy;

import brave.Span;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceInfo;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.serial.ByteRpcContent;
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
import org.springframework.util.Assert;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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
            Integer stateful = export.isStateful() ? 1 : 0;
            ServiceInfo serviceInfo = new ServiceInfo(tag, serviceHostName, port, stateful);
            serviceStateMgr.setLocalServiceInfo(tag, serviceInfo);
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
                "intercept service {}, method {} class {}",
                tagName,
                methodName,
                declaringClass.getSimpleName());
//        CallQueue currentCallQueue = CallQueueMgr.getInstance().getLocalQueue();
//        Assert.notNull(currentCallQueue,"rpc callQueue is null methodName " + methodName);
//        Span currentSpan = ZipkinUtil.currentSpan();
        return GetBeanUtil.getBean(ServiceStateMgr.class).getServiceInfo(tagName)
                .nextDo(serviceInfo -> {
                    return ExchangeHostName.exchange(serviceInfo, objects)
                            .nextDo(realHostName -> {
                                if (!StringUtils.isEmpty(realHostName)) {
                                    boolean isStateful = ServiceUtil.isStatefulService(realHostName);
                                    RpcContent callContent = rpcHandlerInfoForClient.serializeParamForInvoke(methodName, objects);
                                    return rpcClientMgr
                                            .getGrpcAgentClient(realHostName, isStateful)
                                            .rpcCall(methodName, callContent)
//                                            .switchThread(currentCallQueue,currentSpan)
                                            .nextDo(ret -> {
                                                return processReturn(methodName, (Tuple2<String, ByteRpcContent>) ret);
                                            })
                                            .catchError(throwable -> {
                                                log.error("rpc client call throwable {}", throwable);
                                                HomoError.throwError(HomoError.rpcAgentTypeNotSupport);
                                            })
                                            ;
                                } else {
                                    return Homo.result(HomoError.throwError(HomoError.hostNotFound, tagName));
                                }
                            });
                });
    }

    @NotNull
    private Homo processReturn(String methodName, Tuple2<String, ByteRpcContent> ret) throws HomoException {
        Tuple2<String, ByteRpcContent> retTuple = ret;
        String msgId = retTuple.getT1();
        ByteRpcContent rpcContent = retTuple.getT2();
        if (!msgId.equals(methodName)) {//返回的msgId不等于方法名  抛出异常
            return Homo.error(HomoError.throwError(Integer.parseInt(msgId), msgId));
        }
        Object[] param = rpcHandlerInfoForClient.unSerializeParamForCallback(methodName, rpcContent);
        Homo returnHomo;
        /**
         * 如果返回值是空 或 长度为0，返回null
         * 如果返回值长度为1，返回第一个元素
         * 如果返回值是数组，返回Tuples
         */
        if (param == null || param.length == 0) {
            returnHomo = Homo.result(null);
        } else if (param.length <= 1) {
            returnHomo = Homo.result(param[0]);
        } else {
            returnHomo = Homo.result(Tuples.fromArray(param));
        }
        return returnHomo;
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
