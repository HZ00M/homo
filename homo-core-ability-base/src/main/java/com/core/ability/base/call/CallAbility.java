package com.core.ability.base.call;

import brave.Span;
import com.core.ability.base.AbstractAbility;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.ability.ICallAbility;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.rpc.base.serial.RpcHandlerInfoForServer;
import com.homo.core.rpc.base.serial.ByteRpcContent;
import com.homo.core.rpc.base.service.CallDispatcher;
import com.homo.core.utils.concurrent.queue.IdCallQueue;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程调用能力实现
 */
public class CallAbility extends AbstractAbility implements ICallAbility {
    public static Map<Class<?>, CallDispatcher> entityDispatcherMap = new ConcurrentHashMap<>();

    public CallAbility(AbilityEntity abilityEntity) {
        attach(abilityEntity);
    }

    public void afterAttach(AbilityEntity abilityEntity) {
        CallSystem callSystem = GetBeanUtil.getBean(CallSystem.class);
        callSystem.add(this).start();
    }

    @Override
    public void unAttach(AbilityEntity abilityEntity) {
        log.info("CallAbility unAttach");
        CallSystem callSystem = GetBeanUtil.getBean(CallSystem.class);
        callSystem.remove(this).start();
    }

    public static CallDispatcher getEntityCallDispatcher(Class<?> entityClazz) {
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
        return callDispatcher.callFun(getOwner(), srcName, funName, new ByteRpcContent(data, RpcContentType.BYTES, span), idCallQueue, queueId);
    }


}
