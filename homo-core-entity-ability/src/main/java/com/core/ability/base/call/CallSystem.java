package com.core.ability.base.call;

import brave.Span;
import brave.Tracer;
import com.google.protobuf.ByteString;
import com.homo.core.configurable.ability.AbilityProperties;
import com.homo.core.facade.ability.*;
import com.homo.core.facade.service.Service;
import com.homo.core.facade.service.ServiceInfo;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.base.utils.ServiceUtil;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.queue.IdCallQueue;
import com.homo.core.utils.lang.KKMap;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.module.ServiceModule;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程调用系统
 */

@Slf4j
public class CallSystem implements ICallSystem, ServiceModule {
    IdCallQueue idCallQueue = new IdCallQueue("CallSystem", 5000, IdCallQueue.DropStrategy.DROP_CURRENT_TASK, 3);
    @Autowired
    ServiceMgr serviceMgr;
    @Autowired
    ServiceStateMgr serviceStateMgr;
    @Autowired
    AbilityProperties abilityProperties;
    @Autowired
    private RootModule rootModule;
    Map<String, Boolean> methodInvokeByQueueMap = new ConcurrentHashMap<>();
    KKMap<String, String, ICallAbility> type2id2callAbilityMap = new KKMap<>();
    KKMap<String, String, Boolean> id2type2callLinkMap = new KKMap<>();

    @Override
    public void afterAllModuleInit() {
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
            for (Method method : entityClazz.getMethods()) {
                methodInvokeByQueueMap.put(getMethodInvokeByQueueMapKey(entityType.type(), method.getName()), method.getAnnotation(InvokeByQueue.class) != null);
            }
            String serviceHost = ServiceUtil.getServiceHostNameByTag(mainService.getTagName());
            int servicePort = ServiceUtil.getServicePortByTag(mainService.getTagName());
            ServiceInfo serviceInfo = new ServiceInfo(mainService.getTagName(), serviceHost, servicePort, mainService.isStateful(), mainService.getType().ordinal());
            serviceStateMgr.setServiceInfo(entityType.type(), serviceInfo)
                    .catchError(throwable -> {
                        log.error("setServiceNameTag error entity {}", entityType.type(), throwable);
                    })
                    .start();
        }
    }

    @Override
    public void beforeClose() {
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
        abilityEntityMgr.registerAddProcess(CallAble.class, CallAbility::new);
    }

    @Override
    public Homo call(String srcName, EntityRequest entityRequest, Integer podId, ParameterMsg parameterMsg) throws Exception {
        List<ByteString> paramBytesList = entityRequest.getContentList();
        byte[][] paramArr = new byte[paramBytesList.size()][];
        for (int i = 0; i < paramBytesList.size(); i++) {
            byte[] bytes = paramBytesList.get(i).toByteArray();
            paramArr[i] = bytes;
        }
        String type = entityRequest.getType();
        String id = entityRequest.getId();
        String funName = entityRequest.getFunName();
        CallQueue callQueue = CallQueueMgr.getInstance().getLocalQueue();
        Span span = ZipkinUtil.getTracing().tracer().currentSpan().name(funName).tag("entityType", type).tag("entityId", id);
        return GetBeanUtil.getBean(AbilityEntityMgr.class)
                .getEntityPromise(type, id)
                .switchThread(callQueue, span)
                .nextDo(abilityEntity -> {
                    CallAbility callAbility = abilityEntity.getAbility(CallAbility.class);
                    return callAbility.callEntity(srcName, funName, paramArr, podId, parameterMsg, idCallQueue, abilityEntity.getQueueId())
                            .nextDo(logicData -> {
                                byte[] logicDataArr = (byte[]) logicData;
                                EntityResponse.Builder builder = EntityResponse.newBuilder();
                                builder.addContent(ByteString.copyFrom(logicDataArr));
                                builder.setType(type)
                                        .setId(id)
                                        .setSession(entityRequest.getSession())
                                        .setFunName(funName).build();
                                return Homo.result(builder.build());
                            });
                });
//                .finallySignal(ret->{
//                    span.finish();
//                });
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
                        if (CallQueueMgr.getInstance().getLocalQueue().getId() != abilityEntity.getQueueId()) {
                            // 如果当前线程不是目标所在线程，就需要切换到目标所在线程
                            // 这里要避免调用call(Callable<R> callable, int queueId) 因为sink的是callable返回值而不是业务方法的homo返回值
                            entityTpfPromise = CallQueueMgr.getInstance().call(
                                Homo.warp(() -> {
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
                                }), abilityEntity.getQueueId());
                        } else {
                            //用户指定线程就是当前线程，直接调用 业务方法
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
                        }

                        if (methodInvokeByQueueMap.containsKey(entityMethodName)) {
                            return Homo.queue(idCallQueue, () -> entityTpfPromise, () -> log.error("callLocalMethod queue time out. type: {}, id: {}, funName: {}", type, id, method.getName()));
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

    @Override
    public Homo<Boolean> add(ICallAbility callAbility) {
        log.info("CallSystem add entity type {} id {}", callAbility.getOwner().getType(), callAbility.getOwner().getId());
        String type = callAbility.getOwner().getType();
        String id = callAbility.getOwner().getId();
        //保存callAbility引用
        type2id2callAbilityMap.set(type, id, callAbility);
        //设置连接信息
        boolean linked = id2type2callLinkMap.containsFirstKey(id);
        if (linked) {
            log.info("CallSystem no need to link when add entity type {} id {}", type, id);
            return Homo.result(true);
        } else {
            return serviceStateMgr.setUserLinkedPod(id, rootModule.getServerInfo().serverName, serviceStateMgr.getPodIndex(), false)
                    .nextValue(prePodIndex -> {
                        log.info("CallSystem setUserLinkedPod type {} id {} prePodIndex {} podIndex {} ", type, id, prePodIndex, serviceStateMgr.getPodIndex());
                        //如果之前的podIndex不为空，且不等于当前podIndex，则添加失败
                        if (prePodIndex != null && !prePodIndex.equals(serviceStateMgr.getPodIndex())) {
                            log.error("CallSystem setUserLinkedPod type {} id {} prePodIndex not equals current podIndex prePodIndex {} currentPodIndex {}", type, id, prePodIndex, serviceStateMgr.getPodIndex());
                            return false;
                        }
                        return true;
                    });
        }
    }

    @Override
    public Homo<ICallAbility> remove(ICallAbility callAbility) {
        AbilityEntity owner = callAbility.getOwner();
        String type = owner.getType();
        String id = owner.getId();
        type2id2callAbilityMap.remove(type, id);
        //去除连接信息
        id2type2callLinkMap.remove(id, type);
        if (!id2type2callLinkMap.containsFirstKey(id)) {
            return serviceStateMgr.removeUserLinkedPod(id, rootModule.getServerInfo().serverName, false).nextValue(ret -> callAbility);
        } else {
            return Homo.result(callAbility);
        }
    }

    @Override
    public ICallAbility get(String type, String id) {
        return type2id2callAbilityMap.get(type, id);
    }

}
