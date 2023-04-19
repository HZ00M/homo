package com.core.ability.base.call;

import brave.Span;
import com.core.ability.base.AbstractAbility;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.serial.RpcContentType;
import com.homo.core.rpc.base.serial.RpcHandlerInfoForServer;
import com.homo.core.rpc.base.serial.TraceRpcContent;
import com.homo.core.rpc.base.service.CallDispatcher;
import com.homo.core.utils.concurrent.queue.IdCallQueue;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallAbility extends AbstractAbility {
    static Map<Class<?>, CallDispatcher> entityDispatcherMap = new ConcurrentHashMap<>();

    public CallAbility(AbilityEntity abilityEntity) {
        attach(abilityEntity);
    }

    @Override
    public void attach(AbilityEntity abilityEntity) {
        log.info("CallAbility attach");
    }

    public CallDispatcher getEntityCallDispatcher(Class<? extends AbilityEntity> entityClazz) {
        return entityDispatcherMap.computeIfAbsent(entityClazz, k -> {
            Class<?> entityInterface = HomoAnnotationUtil.findAnnotationInterface(entityClazz, EntityType.class);
            if (entityInterface == null) {
                log.error("cant build entity call dispatcher, entityClazz_{}", entityClazz);
                return null;
            }
            Assert.isTrue(entityInterface.isInterface(), "can't build entity call dispatcher, entityInterface_{} is not interface");
            try {
                RpcHandlerInfoForServer rpcHandlerInfoForServer = new RpcHandlerInfoForServer(entityClazz);
                CallDispatcher callDispatcher = new CallDispatcher(rpcHandlerInfoForServer);
                return callDispatcher;
            } catch (Exception e) {
                log.error("exportFunction error!", e);
                return null;
            }
        });
    }

    public Homo callEntity(String srcName, String funName, byte[][] data, Integer podId, Object parameterMsg, IdCallQueue idCallQueue, Integer queueId) {
        CallDispatcher callDispatcher = getEntityCallDispatcher(getOwner().getClass());
        Assert.isTrue(callDispatcher != null, "CallAbility build callDispatcher is null ");
        Span span = ZipkinUtil.getTracing().tracer().currentSpan().tag("callEntity", funName);
        return callDispatcher.callFun(getOwner(), srcName, funName, new TraceRpcContent(data, RpcContentType.BYTES, span), idCallQueue, queueId);
    }

    @Override
    public void unAttach(AbilityEntity abilityEntity) {
        log.info("CallAbility unAttach");
    }
}
