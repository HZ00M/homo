package com.core.ability.base;

import brave.Span;
import com.core.ability.base.storage.SaveAble;
import com.core.ability.base.storage.StorageSystem;
import com.homo.core.common.module.ServiceModule;
import com.homo.core.configurable.ability.AbilityProperties;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.facade.ability.AbilitySystem;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.utils.concurrent.lock.Locker;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class StorageEntityMgr extends CacheEntityMgr implements ServiceModule {
    @Autowired
    AbilityProperties abilityProperties;
    @Autowired
    StorageSystem storageSystem;
    Map<Class<? extends AbilitySystem>, AbilitySystem> systemMap = new HashMap<>();
    static Map<String, Class<AbilityEntity>> typeToAbilityObjectClazzMap = new ConcurrentHashMap<>();

    @Autowired
    StorageEntityMgr(Set<? extends AbilitySystem> abilitySystems) {
        init(abilitySystems);
    }

    Locker locker = new Locker();

    protected void init(Set<? extends AbilitySystem> abilitySystems) {
        for (AbilitySystem system : abilitySystems) {
            system.init(this);
            systemMap.put(system.getClass(), system);
        }
        String entityScanPath = abilityProperties.getEntityScanPath();
        Reflections reflections = new Reflections(entityScanPath);
        Set<Class<?>> entityClazzSet = reflections.getTypesAnnotatedWith(EntityType.class);
        for (Class<?> entityClazz : entityClazzSet) {
            EntityType entityType = entityClazz.getAnnotation(EntityType.class);
            String type = entityType.type();
            typeToAbilityObjectClazzMap.computeIfAbsent(type, k -> (Class<AbilityEntity>) entityClazz);
            typeToAbilityObjectClazzMap.computeIfPresent(type, (k, oldClazz) -> {
                if (!entityClazz.equals(oldClazz) && oldClazz.isAssignableFrom(entityClazz)) {//新的class是老的class的子类，代替老的class
                    return (Class<AbilityEntity>) entityClazz;
                } else {
                    return oldClazz;
                }
            });
        }
    }

    <T extends AbilityEntity> Homo<T> asyncGet(Class<T> clazz, String id) {
        return Homo.queue(idCallQueue, () -> {
            Homo<T> ret;
            T entity = get(clazz, id);
            if (entity != null) {
                ret = Homo.result(entity);
            } else {
                ret = asyncLoad(clazz, id);
            }
            if (entity != null) {
                ret.switchThread(entity.getQueueId());
            }
            return ret;
        }, () -> log.error("asyncGet error clazz {} id {}", clazz, id));
    }

    <T extends AbilityEntity> Homo<T> asyncLoad(Class<T> clazz, String id) {
        if (clazz == null) {
            log.error("asyncLoad clazz is null id {}", id);
        }
        assert clazz != null;
        if (!SaveAble.class.isAssignableFrom(clazz)) {
            //没有存储能力，返回空
            return Homo.result(null);
        } else {
            Span span = ZipkinUtil.getTracing()
                    .tracer()
                    .nextSpan()
                    .name("asyncLoad")
                    .annotate(ZipkinUtil.CLIENT_SEND_TAG);
            return storageSystem.loadEntity(clazz, id)
                    .nextDo(entity ->
                            locker.lockCallable(id, () -> {
                                log.trace("asyncLoad finished, clazz_{} id_{} entity_{}", clazz, id, entity);
                                T inMenEntity = get(clazz, id);
                                if (inMenEntity != null) {
                                    //内存有了就直接使用
                                    return Homo.result(inMenEntity);
                                }
                                if (entity != null) {
                                    return entity.promiseInit().nextDo(ret-> Homo.result(entity));
                                }
                                return Homo.result(null);
                            }))
                    .finallySignal(signalType -> {
                        span.tag(ZipkinUtil.FINISH_TAG, "asyncLoad")
                                .annotate(ZipkinUtil.CLIENT_RECEIVE_TAG)
                                .finish();
                    });
        }
    }

    public <T extends AbilityEntity> Homo<T> asyncGetOrCreate(Class<T> clazz, String id, Object... params) {
        return asyncGet(clazz, id)
                .nextDo(entity -> {
                    if (entity != null) {
                        return Homo.result(entity);
                    } else {
                        return createEntityPromise(clazz, id, params);
                    }
                });
    }

    @Override
    public void close() {
        removeAllEntity();
        assert storageSystem.allEntityLanded();
    }
}
