package com.core.ability.base.call;

import brave.Span;
import brave.Tracer;
import com.google.protobuf.ByteString;
import com.homo.core.common.module.ServiceModule;
import com.homo.core.configurable.ability.AbilityProperties;
import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.ability.InvokeByQueue;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceStateHandler;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.queue.IdCallQueue;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.entity.EntityRequest;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class CallSystem implements ICallSystem, ServiceModule {
    IdCallQueue idCallQueue = new IdCallQueue("CallSystem", 5000, IdCallQueue.DropStrategy.DROP_CURRENT_TASK);
    @Autowired
    ServiceMgr serviceMgr;
    @Autowired
    ServiceStateHandler serviceStateHandler;
    @Autowired
    AbilityProperties abilityProperties;
    Map<String, Boolean> methodInvokeByQueueMap = new ConcurrentHashMap<>();

    @Override
    public void init() {
        //将本服务的entity type 映射到主服务上，为其他服进行远程调用提供支持
        Reflections reflections = new Reflections(abilityProperties.getEntityScanPath());
        Set<Class<?>> entitySet = reflections.getTypesAnnotatedWith(EntityType.class);
        Service mainService = serviceMgr.getMainService();
        for (Class<?> entityClazz : entitySet) {
            if (!entityClazz.isInterface()) {
                //不是接口跳过
                continue;
            }
            if (reflections.getSubTypesOf(entityClazz).isEmpty()) {
                //没有实现类跳过
                continue;
            }
            EntityType entityType = entityClazz.getAnnotation(EntityType.class);
            if (entityType.isLocalService()) {
                //本地服务跳过
                continue;
            }
            for(Method method : entityClazz.getMethods()){
                methodInvokeByQueueMap.put(getMethodInvokeByQueueMapKey(entityType.type(), method.getName()), method.getAnnotation(InvokeByQueue.class) != null);
            }
            serviceStateHandler.setServiceNameTag(entityType.type(), mainService.getHostName())
                    .catchError(throwable -> {
                        log.error("setServiceNameTag error entity {}", entityType.type(), throwable);
                    })
                    .start();
        }
    }

    @Override
    public void close() {
        //注册拒绝请求回调
//        GetBeanUtil.getBean(RpcServerMgr.class).delegate.addToTail((srcService, funName, param) -> {
//            if(funName.equals(EntityConstant.ENTITY_CALL_METHOD_NAME)){
//                log.info("onEntityCall deny!");
//                if(RpcContentType.BYTES == param.getType()){
//                    try {
//                        EntityRequest entityRequest = EntityRequest.parseFrom(((BytesArrayRpcContent) param).getData()[1]);
//                        log.info("onEntityCall deny entityRequest: {} {}",entityRequest.getType(),entityRequest.getId());
//                        tryToRemoveEntityLinkInfo(entityRequest.getType(),entityRequest.getId());
//                    } catch (InvalidProtocolBufferException e) {
//                        log.error("EntityRequest paser error srcSvc:{} fun:{} param:{}",srcService,funName,param);
//                    }
//                }
//            }
//        });
    }

    @Override
    public void init(AbilityEntityMgr abilityEntityMgr) {
        abilityEntityMgr.registerAddProcess(CallAbility.class, CallAbility::new);
    }

    @Override
    public Homo call(String srcName, EntityRequest entityRequest, Integer podId, Object parameterMsg) throws Exception {
        List<ByteString> paramBytesList = entityRequest.getContentList();
        byte[][] paramArr = new byte[paramBytesList.size()][];
        for (int i = 0; i < paramBytesList.size(); i++) {
            byte[] bytes = paramBytesList.get(i).toByteArray();
            paramArr[i] = bytes;
        }
        String type = entityRequest.getType();
        String id = entityRequest.getId();
        String funName = entityRequest.getFunName();
        ZipkinUtil.getTracing().tracer().currentSpan().name(funName).tag("entityType", type).tag("entityId", id);
        return GetBeanUtil.getBean(AbilityEntityMgr.class)
                .getEntityPromise(type, id)
                .nextDo(abilityEntity -> {
                    CallAbility callAbility = abilityEntity.getAbility(CallAbility.class);
                    return callAbility.callEntity(srcName, funName, paramArr, podId, parameterMsg, idCallQueue, abilityEntity.getQueueId());
                });
    }

    private String getMethodInvokeByQueueMapKey(String type, String funName) {
        return type + "_" + funName;
    }

    @Override
    public Homo callLocalMethod(String type, String id, Method method, Object[] objects) {

        return GetBeanUtil.getBean(AbilityEntityMgr.class)
                .getEntityPromise(type, id)
                .nextDo(abilityEntity -> {
                    if (abilityEntity == null) {
                        return Homo.error(new RuntimeException("abilityEntity is null type_" + type + " id_" + id));
                    }
                    Span span =
                            ZipkinUtil.getTracing()
                                    .tracer()
                                    .nextSpan()
                                    .tag("start", "callLocalMethod")
                                    .tag("type", abilityEntity.getType())
                                    .tag("id", abilityEntity.getId())
                                    .annotate(ZipkinUtil.SERVER_SEND_TAG)
                                    .name(method.getName());
                    Homo<Object> entityTpfPromise;
                    String entityMethodName = getMethodInvokeByQueueMapKey(type, method.getName());
                    try (Tracer.SpanInScope ws = ZipkinUtil.getTracing().tracer().withSpanInScope(span)) {
                        //用户指定线程就是当前线程，直接调用
                        entityTpfPromise = Homo.warp(() -> {
                            try {
                                Object rel = method.invoke(abilityEntity, objects);
                                if (Homo.class.equals(method.getReturnType())) {
                                    return (Homo<Object>) rel;
                                } else if (rel != null) {
                                    return Homo.result(rel);
                                } else {
                                    return Homo.result();
                                }
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                return Homo.error(e);
                            }
                        });
                        if (CallQueueMgr.getInstance().getLocalQueue().getId() != abilityEntity.getQueueId()) {
                            // 如果当前线程不是目标所在线程，就需要切换到目标所在线程
                            Homo<Object> oldPromise = entityTpfPromise;
                            entityTpfPromise = CallQueueMgr.getInstance().call(oldPromise::start, abilityEntity.getQueueId());
                        }
                        if (methodInvokeByQueueMap.containsKey(entityMethodName)) {
                            Homo<Object> finalEntityTpfPromise = entityTpfPromise;
                            return Homo.queue(idCallQueue, () -> finalEntityTpfPromise, () -> log.error("callLocalMethod queue time out. type: {}, id: {}, funName: {}", type, id, method.getName()));
                        } else {
                            return entityTpfPromise.catchError(throwable -> {
                                log.error("callLocalMethod asyncGet", throwable);
                            }).finallySignal(signalType -> {
                                span.tag(ZipkinUtil.FINISH_TAG, signalType.toString()).annotate(ZipkinUtil.SERVER_RECEIVE_TAG).finish();
                            });
                        }
                    }
                });
    }

}
