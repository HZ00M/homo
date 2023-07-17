package com.core.ability.base;

import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.service.ServiceStateMgr;
import com.homo.core.utils.concurrent.queue.IdCallQueue;
import com.homo.core.utils.lang.KKMap;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import com.homo.core.utils.reflect.HomoInterfaceUtil;
import com.homo.core.utils.spring.GetBeanUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 具备缓存能力的对象实体管理器
 */
@Log4j2
public class CacheEntityMgr implements AbilityEntityMgr {
    static Map<Class<?>, Class<?>[]> entityClazz2ParamsMap = new ConcurrentHashMap<>();
    IdCallQueue idCallQueue = new IdCallQueue("AbilityObjEntityImpl");
    @Getter
    AtomicInteger entityCount = new AtomicInteger(0);
    KKMap<String, String, AbilityEntity> type2Id2ObjMap = new KKMap<>();
    Map<Class<?>, Consumer<AbilityEntity>> createProcess = new HashMap<>();
    Map<Class<?>, Consumer<AbilityEntity>> addProcess = new HashMap<>();
    Map<Class<?>, Consumer<AbilityEntity>> getProcess = new HashMap<>();
    Map<Class<?>, Consumer<AbilityEntity>> removeProcess = new HashMap<>();

    private <T extends AbilityEntity> void processConsumer(Map<Class<?>, Consumer<AbilityEntity>> consumerMap, T abilityEntity) {
        Set<Class<?>> allInterfaces = HomoInterfaceUtil.getAllInterfaces(abilityEntity.getClass());
        for (Class<?> interfaceClazz : allInterfaces) {
            consumerMap.computeIfPresent(interfaceClazz, (k, v) -> {
                v.accept(abilityEntity);
                return v;
            });
        }
    }

    @Override
    public <T extends AbilityEntity> boolean add(T abilityEntity) {
        log.info("add entity start, type_{} id_{}", abilityEntity.getType(), abilityEntity.getId());
        AbilityEntity objEntity = type2Id2ObjMap.get(abilityEntity.getType(), abilityEntity.getId());
        if (objEntity != null) {
            log.error(
                    "add entity error, type id is already exist ,type_{} id_{}",
                    objEntity.getType(),
                    objEntity.getId(), new Exception("重复添加entity!"));
            return false;
        }
        processConsumer(addProcess, abilityEntity);
        type2Id2ObjMap.set(abilityEntity.getType(), abilityEntity.getId(), abilityEntity);
        GetBeanUtil.getBean(ServiceStateMgr.class).setLoad(entityCount.incrementAndGet());
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbilityEntity> T get(String type, String id) {
        T abilityEntity = (T) type2Id2ObjMap.get(type, id);
        if (abilityEntity == null) {
            return null;
        }
        processConsumer(getProcess, abilityEntity);
        return abilityEntity;
    }

    @Override
    public <T extends AbilityEntity> T get(Class<T> entityClazz, String id) {
        EntityType entityType = HomoAnnotationUtil.findAnnotation(entityClazz, EntityType.class);
        if (entityType == null) {
            return null;
        }
        return get(entityType.type(), id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbilityEntity> Set<T> getAll(String type) {
        Set<T> allEntity = (Set<T>) type2Id2ObjMap.getAll(type);
        if (allEntity == null) {
            return null;
        }
        for (T t : allEntity) {
            processConsumer(getProcess, t);
        }
        return allEntity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbilityEntity> T remove(String type, String id) {
        T abilityEntity = (T) type2Id2ObjMap.get(type, id);
        if (abilityEntity == null) {
            log.warn("remove entity but not found, type_{}, id_{}", type, id);
        } else {
            processConsumer(removeProcess, abilityEntity);
            type2Id2ObjMap.remove(type, id);
            GetBeanUtil.getBean(ServiceStateMgr.class).setLoad(entityCount.decrementAndGet());
        }
        return abilityEntity;
    }

    @Override
    public <T extends AbilityEntity> T remove(Class<T> entityClazz, String id) {
        EntityType entityType = HomoAnnotationUtil.findAnnotation(entityClazz, EntityType.class);
        if (entityType == null) {
            return null;
        }
        return remove(entityType.type(), id);
    }

    @Override
    public Homo<Boolean> removeAllType(String id) {
        return Homo.queue(idCallQueue, new Callable<Homo<Boolean>>() {
            @Override
            public Homo<Boolean> call() throws Exception {
                Set<String> typeSet = type2Id2ObjMap.getK1Set();
                List<Homo<Void>> destoryList = new ArrayList<>();
                for (String type : typeSet) {
                    AbilityEntity abilityEntity = get(type, id);
                    if (abilityEntity != null) {
                        Homo<Void> promiseDestroy = abilityEntity.promiseDestroy();
                        destoryList.add(promiseDestroy);
                    }
                }
                return Homo.all(destoryList, items -> true);
            }
        }, () -> {
            log.error("removeAllType error id {}", id);
        });
    }

    @Override
    public <T extends AbilityEntity> Homo<T> getOrCreateEntityPromise(Class<T> entityClazz, String id, Object... params) {
        return getEntityPromise(entityClazz, id)
                .nextDo(getEntity -> {
                    if (getEntity == null) {
                        return createEntityPromise(entityClazz, id, params);
                    }
                    return Homo.result(getEntity);
                });
    }

    @Override
    public <T extends AbilityEntity> Homo<T> createEntityPromise(Class<T> entityClazz, String id, Object... params) {
        return Homo.queue(idCallQueue, () -> {
            T entity = get(entityClazz, id);//创建前再次判断是否存在
            if (entity == null) {
                log.info("createEntityPromise star entityClazz {} id {} params {}", entityClazz, id, params);
                entity = newEntity(id, entityClazz, params);
                if (entity == null) {
                    return Homo.error(new Exception("createEntityPromise error id " + id + " entityClazz " + entityClazz));
                }
            }
            T finalEntity = entity;
            return entity.promiseInit()
                    .nextValue(self -> {
                        log.error("createEntityPromise finish id {} entityClazz {} params {} self {}", id, entityClazz, params, self);
                        processConsumer(createProcess, finalEntity);
                        return finalEntity;
                    });
        }, () -> log.error("createEntityPromise fail id {} entityClazz {} params {}", id, entityClazz, params));
    }

    private <T extends AbilityEntity> T newEntity(String id, Class<T> entityClazz, Object... params) {
        log.info("newEntity id {} entityClazz {} params {}", id, entityClazz, params);
        try {
            Class<?>[] paramsClasses = entityClazz2ParamsMap.get(entityClazz);
            if (paramsClasses == null) {
                paramsClasses = new Class<?>[params.length];
                for (int i = 0; i < params.length; i++) {
                    paramsClasses[i] = params[i].getClass();
                }
            }
            Constructor<T> constructor = entityClazz.getConstructor(paramsClasses);
            T newInstance = constructor.newInstance(params);
            if (id != null) {
                newInstance.setId(id);
            }
            return newInstance;
        } catch (Exception e) {
            log.error("newEntity fail, id {} entityClazz {} params {}", id, entityClazz, params, e);
            return null;
        }
    }

    @Override
    public <T extends AbilityEntity> Homo<T> getEntityPromise(String type, String id) {
        return Homo.result(get(type, id));
    }

    @Override
    public <T extends AbilityEntity> Homo<T> getEntityPromise(Class<T> entityClazz, String id) {
        log.info("getEntityPromise  clazz {} id {}", entityClazz, id);
        EntityType entityType = HomoAnnotationUtil.findAnnotation(entityClazz, EntityType.class);
        if (entityType == null) {
            log.error("getEntityPromise entityType is null,clazz {} id {}", entityClazz, id);
            return null;
        }
        return getEntityPromise(entityType.type(), id);
    }

    @Override
    public void removeAllEntity() {
        for (AbilityEntity abilityEntity : type2Id2ObjMap.getAll()) {
            abilityEntity.promiseDestroy().start();
        }
    }

    @Override
    public void registerCreateProcess(Class<?> clazz, Consumer<AbilityEntity> consumer) {
        createProcess.put(clazz, consumer);
    }

    @Override
    public void registerAddProcess(Class<?> clazz, Consumer<AbilityEntity> consumer) {
        addProcess.put(clazz, consumer);
    }

    @Override
    public void registerGetProcess(Class<?> clazz, Consumer<AbilityEntity> consumer) {
        getProcess.put(clazz, consumer);
    }

    @Override
    public void registerRemoveProcess(Class<?> clazz, Consumer<AbilityEntity> consumer) {
        removeProcess.put(clazz, consumer);
    }


}
