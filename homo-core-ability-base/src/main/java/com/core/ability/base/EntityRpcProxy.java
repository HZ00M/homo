package com.core.ability.base;

import com.core.ability.base.call.CallAbility;
import com.core.ability.base.call.CallSystem;
import com.google.protobuf.ByteString;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.ability.ICallAbility;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.serial.ByteRpcContent;
import com.homo.core.rpc.base.service.CallDispatcher;
import com.homo.core.rpc.client.RpcHandlerInfoForClient;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.entity.EntityRequest;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class EntityRpcProxy implements MethodInterceptor {
    private String serviceName;
    private String type;
    @Setter
    private String id;
    private RpcAgentClient rpcAgentClient;
    private Class<?> interfaceType;
    static Map<Class<?>, RpcHandlerInfoForClient> rpcHandlerInfoForClientMap = new ConcurrentHashMap<>();
    private CallSystem callSystem;
    private ServiceStateMgr serviceStateMgr;
    RpcHandlerInfoForClient handleInfo;

    public <T> EntityRpcProxy(RpcAgentClient rpcAgentClient, Class<T> entityHandlerInterface, String id, String serviceName) {
        this.serviceName = serviceName;
        this.callSystem = GetBeanUtil.getBean(CallSystem.class);
        this.rpcAgentClient = rpcAgentClient;
        this.type = entityHandlerInterface.getAnnotationsByType(EntityType.class)[0].type();
        this.id = id;
        this.interfaceType = entityHandlerInterface;
        this.handleInfo = rpcHandlerInfoForClientMap.compute(entityHandlerInterface, (k, v) -> {
            if (v == null) {
                return new RpcHandlerInfoForClient(entityHandlerInterface);
            }
            return v;
        });
    }

    public Object getProxyInstance() {
        //cglib需要继承父类,java InvokerHandler需要实现接口
        Enhancer enhancer = new Enhancer();
        //设置父类
        enhancer.setSuperclass(interfaceType);
        //设置回调函数
        enhancer.setCallback(this);
        //创建子类对象代理
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (!method.getDeclaringClass().isAssignableFrom(interfaceType)) {
            log.trace("intercept super invoke, method_{}", method);
            return methodProxy.invokeSuper(o, objects);
        }

        // 如果有CallSystemImpl实现类，且是本地服务的entity call，则使用本地call,否则用rpc call
        if (callSystem != null && callSystem.get(type, id) != null) {
            return callSystem.callLocalMethod(type, id, method, objects);
        }
        //发起远程调用
        String methodName = method.getName();
        EntityRequest.Builder builder = EntityRequest.newBuilder().setType(type).setId(id).setFunName(methodName);
        ByteRpcContent rpcContent = (ByteRpcContent) rpcHandlerInfoForClientMap.get(o.getClass()).serializeParamForInvoke(methodName,objects);
        if (rpcContent != null) {
            byte[][] data = rpcContent.getData();
            if (data!= null){
                for (byte[] datum : data) {
                    builder.addContent(ByteString.copyFrom(datum));
                }
            }
        }
        EntityRequest entityRequest = builder.build();
        return rpcCall(type,id,methodName,entityRequest);
    }

    private Object rpcCall(String type,String id,String methodName,EntityRequest entityRequest) throws Exception {
        ICallAbility callAbility = callSystem.get(type, id);
        CallDispatcher callDispatcher = CallAbility.entityDispatcherMap.get(callAbility.getOwner().getClass());
        RpcContent rpcContent = callDispatcher.rpcHandleInfo.getMethodDispatchInfo(methodName).serializeParam(new Object[]{entityRequest});
        return Homo.warp(homoSink -> {
            if (serviceName == null) {
                serviceStateMgr.getServiceNameByTag(type)
                        .consumerValue(serviceNameByTag -> {
                            if (type.equals(serviceNameByTag)) {
                                log.error("entity proxy rpcClientCall getServiceNameByTag is null. type: {}, id: {}, funName: {}", type, id, methodName);
                                homoSink.error(new Throwable("entity proxy rpcClientCall getServiceNameByTag is null"));
                            }
                            rpcAgentClient.rpcCall(methodName,rpcContent)
                                    .consumerValue(ret->{
                                        homoSink.success(ret);
                                        ZipkinUtil.getTracing().tracer().currentSpan().tag("entityType", type)
                                                .tag("entityId", id)
                                                .tag("methodName", methodName);
                                    }).start();
                        });
            }else {
                rpcAgentClient.rpcCall(methodName,rpcContent)
                        .consumerValue(ret->{
                            homoSink.success(ret);
                            ZipkinUtil.getTracing().tracer().currentSpan().tag("entityType", type)
                                    .tag("entityId", id)
                                    .tag("methodName", methodName);
                        }).start();
            }
        });
    }
}
