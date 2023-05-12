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
import com.homo.core.utils.fun.Func2Ex;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 具备存储能力的实体管理器
 */
@Log4j2
public class StorageEntityMgr extends CacheEntityMgr implements ServiceModule {
    static Map<String, Class<AbilityEntity>> typeToAbilityObjectClazzMap = new ConcurrentHashMap<>();
    AbilityProperties abilityProperties;
    @Autowired
    StorageSystem storageSystem;
    Map<Class<? extends AbilitySystem>, AbilitySystem> systemMap = new HashMap<>();
    Locker locker = new Locker();

    @Autowired
    public StorageEntityMgr(Set<? extends AbilitySystem> abilitySystems, AbilityProperties abilityProperties) {
        this.abilityProperties = abilityProperties;
        init(abilitySystems);
    }


    protected void init(Set<? extends AbilitySystem> abilitySystems) {
        for (AbilitySystem system : abilitySystems) {
            system.init(this);
            systemMap.put(system.getClass(), system);
        }
        String entityScanPath = abilityProperties.getEntityScanPath();
        Reflections reflections = new Reflections(entityScanPath);
        Set<Class<?>> entityClazzSet = reflections.getTypesAnnotatedWith(EntityType.class);
        for (Class<?> entityClazz : entityClazzSet) {
            EntityType entityType = HomoAnnotationUtil.findAnnotation(entityClazz, EntityType.class);
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


    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbilityEntity> Homo<T> getEntityPromise(String type, String id) {
        return Homo.queue(idCallQueue, () -> {
            Homo<T> ret;
            T entity = get(type, id);
            if (entity != null) {
                log.info("getEntityPromise entity != null, type {} id {}", type, id);
                ret = Homo.result(entity);
            } else {
                Class<AbilityEntity> entityClazz = typeToAbilityObjectClazzMap.get(type);
                log.info("getEntityPromise asyncLoad, type {} id {}", type, id);
                ret = asyncLoad((Class<T>) entityClazz, id);
            }
            return ret.nextDo(e -> {
                if (e != null) {
                    return Homo.result(e).switchThread(e.getQueueId());
                } else {
                    return Homo.result(e);
                }
            });
        }, () -> log.error("asyncGet error clazz {} id {}", type, id));
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
                                log.info("asyncLoad finished, clazz_{} id_{} entity_{}", clazz, id, entity);
                                T inMenEntity = get(clazz, id);
                                if (inMenEntity != null) {
                                    //内存有了就直接使用
                                    return Homo.result(inMenEntity);
                                }
                                if (entity != null) {
                                    return entity.promiseInit().nextDo(ret -> {
                                        log.info("asyncLoad entity.promiseInit finish, clazz_{} id_{} entity_{}", clazz, id, entity);
                                        return Homo.result(entity);
                                    });
                                } else {
                                    Func2Ex<Class<? extends AbilityEntity>, String, Homo<? extends AbilityEntity>> createFun = notFoundCreateFunMap.get(clazz);
                                    if (createFun != null) {
                                        //如果注册了创建函数，则调用默认创建函数
                                        return (Homo<T>) createFun.apply(clazz, id);
                                    } else {
                                        return Homo.result(null);
                                    }
                                }

                            }))
                    .finallySignal(signalType -> {
                        span.tag(ZipkinUtil.FINISH_TAG, "asyncLoad")
                                .annotate(ZipkinUtil.CLIENT_RECEIVE_TAG)
                                .finish();
                    });
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends AbilityEntity> Homo<T> asyncGetOrCreate(String type, String id, Object... params) {
        return (Homo<T>) getEntityPromise(type, id)
                .nextDo(entity -> {
                    if (entity != null) {
                        return Homo.result(entity);
                    } else {
                        Class<AbilityEntity> entityClass = typeToAbilityObjectClazzMap.get(type);
                        return createEntityPromise(entityClass, id, params);
                    }
                });
    }

    @Override
    public void close() {
        removeAllEntity();
        assert storageSystem.allEntityLanded();
    }
}
