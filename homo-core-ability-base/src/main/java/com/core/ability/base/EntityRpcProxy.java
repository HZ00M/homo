package com.core.ability.base;

import brave.Span;
import brave.Tracer;
import com.core.ability.base.call.CallSystem;
import com.google.protobuf.ByteString;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.ability.IEntityService;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.serial.ByteRpcContent;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.client.ExchangeHostName;
import com.homo.core.rpc.client.RpcClientMgr;
import com.homo.core.rpc.client.RpcHandlerInfoForClient;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.EntityResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程调用代理，提供实体远程调用能力
 */
@Slf4j
public class EntityRpcProxy implements MethodInterceptor {
    private String serviceName;
    private String type;
    @Setter
    private String id;
    //    private RpcAgentClient rpcAgentClient;
    private Class<?> interfaceType;
    static Map<Class<?>, RpcHandlerInfoForClient> rpcHandlerInfoForClientMap = new ConcurrentHashMap<>();
    private CallSystem callSystem;
    private ServiceStateMgr serviceStateMgr;
    private RpcClientMgr rpcClientMgr;
    private ServiceMgr serviceMgr;
    RpcHandlerInfoForClient logicHandlerInfo;
    RpcHandlerInfoForClient serviceEntityRpcInfo;

    public <T> EntityRpcProxy(RpcClientMgr rpcClientMgr, Class<T> entityHandlerInterface, String id, String serviceName) {
        this.serviceName = serviceName;
        this.callSystem = GetBeanUtil.getBean(CallSystem.class);
        this.serviceStateMgr = GetBeanUtil.getBean(ServiceStateMgr.class);
        this.serviceMgr = GetBeanUtil.getBean(ServiceMgr.class);
        this.rpcClientMgr = rpcClientMgr;
        this.type = entityHandlerInterface.getAnnotationsByType(EntityType.class)[0].type();
        this.id = id;
        this.interfaceType = entityHandlerInterface;
        this.serviceEntityRpcInfo = new RpcHandlerInfoForClient(IEntityService.class);
        this.logicHandlerInfo = rpcHandlerInfoForClientMap.compute(entityHandlerInterface, (k, v) -> {
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
    public Homo intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (!method.getDeclaringClass().isAssignableFrom(interfaceType)) {
            log.trace("intercept super invoke, method {}", method);
            return (Homo) methodProxy.invokeSuper(o, objects);
        }

        // 如果有CallSystemImpl实现类，且是本地服务的entity call，则使用本地call,否则用rpc call
        if (callSystem != null && callSystem.get(type, id) != null) {
            log.info("intercept callLocalMethod, methodName {} type {} id {}", method.getName(), type, id);
            return callSystem.callLocalMethod(type, id, method, objects);
        }
        //发起远程调用
        String methodName = method.getName();
        EntityRequest.Builder builder = EntityRequest.newBuilder().setType(type).setId(id).setFunName(methodName);
        ByteRpcContent rpcContent = (ByteRpcContent) rpcHandlerInfoForClientMap.get(interfaceType).serializeParamForInvoke(methodName, objects);
        if (rpcContent != null) {
            byte[][] data = rpcContent.getData();
            if (data != null) {
                for (byte[] datum : data) {
                    builder.addContent(ByteString.copyFrom(datum));
                }
            }
        }
        EntityRequest entityRequest = builder.build();
        log.info("intercept rpcCall, methodName {} type {} id {}", method, type, id);
        return rpcCall(type, id, methodName, entityRequest);
    }

    private Homo rpcCall(String type, String id, String methodName, EntityRequest entityRequest) throws Exception {
        return GetBeanUtil.getBean(ServiceStateMgr.class).getServiceInfo(serviceName)
                .nextDo(serviceInfo -> {
                    if (serviceInfo == null) {
                        log.error("entity proxy rpcClientCall getServiceNameByTag is null. type {} id {} funName {}", type, id, methodName);
                        Homo.error(new Throwable("entity proxy rpcClientCall getServiceNameByTag is null"));
                    } else {
                        serviceName = serviceInfo.getServiceTag();
                    }
                    return ExchangeHostName.exchange(serviceInfo, entityRequest)
                            .nextDo(realName -> {
                                RpcContent rpcContent = serviceEntityRpcInfo.serializeParamForInvoke(IEntityService.default_entity_call_method, new Object[]{-1, entityRequest});
                                return rpcClientMgr.getGrpcAgentClient(realName, true)
                                        .rpcCall(IEntityService.default_entity_call_method, rpcContent)
//                                        .switchThread(currentCallQueue,currentSpan)
                                        .consumerValue(ret -> {
                                            Tuple2<String, RpcContent> msgIdAndContent = (Tuple2<String, RpcContent>) ret;
                                            Object[] entityData = serviceEntityRpcInfo.unSerializeParamForCallback(IEntityService.default_entity_call_method, msgIdAndContent.getT2());
                                            EntityResponse entityResponse = (EntityResponse) entityData[0];
                                            ByteRpcContent logicContent = new ByteRpcContent();
                                            List<ByteString> contentList = entityResponse.getContentList();
                                            byte[][] data = new byte[contentList.size()][];
                                            for (int i = 0; i < contentList.size(); i++) {
                                                data[i] = contentList.get(i).toByteArray();
                                            }
                                            logicContent.setData(data);
                                            Object[] dataObjs = rpcHandlerInfoForClientMap.get(interfaceType).unSerializeParamForCallback(methodName, logicContent);
                                            if (dataObjs == null || dataObjs.length == 0) {
                                                Homo.result(null);
                                            } else if (dataObjs.length <= 1) {
                                                Homo.result(dataObjs[0]);
                                            } else {
                                                Homo.result(Tuples.fromArray(dataObjs));
                                            }
                                        });
                            });
                });
    }
}
