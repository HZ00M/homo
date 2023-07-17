package com.core.ability.base.storage;

import brave.Span;
import com.homo.core.configurable.ability.AbilityProperties;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.facade.ability.AbilitySystem;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.facade.module.ServiceModule;
import com.homo.core.facade.storege.SaveObject;
import com.homo.core.root.storage.ByteStorage;
import com.homo.core.root.storage.ObjStorage;
import com.homo.core.utils.concurrent.lock.Locker;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象自动保存系统
 */
@Log4j2
public class StorageSystem implements AbilitySystem, ServiceModule {
    final Locker locker = new Locker();
    @Autowired
    AbilityProperties abilityProperties;
    @Autowired
    ObjStorage storage;
    @Autowired(required = false)
    HomoSerializationProcessor serializationProcessor;
    HomoTimerMgr timerMgr = HomoTimerMgr.getInstance();

    @Override
    public void init(AbilityEntityMgr abilityEntityMgr) {
        abilityEntityMgr.registerAddProcess(SaveAble.class, StorageAbility::new);

        abilityEntityMgr.registerCreateProcess(SaveAble.class, abilityEntity -> abilityEntity.getAbility(StorageAbility.class).save());
        abilityEntityMgr.registerGetProcess(SaveAble.class, abilityEntity -> abilityEntity.getAbility(StorageAbility.class).currentGet());
    }

    @Data
    static class SaveCache {
        public SaveObject saveObject;
        public byte[] data;
        public boolean isSave;

        public SaveCache(SaveObject saveObject, byte[] data, boolean isSave) {
            this.saveObject = saveObject;
            this.data = data;
            this.isSave = isSave;
        }
    }

    Map<String, SaveCache> saveEntityMap;
    Map<String, SaveCache> survivorEntityMap1 = new ConcurrentHashMap<>();
    Map<String, SaveCache> survivorEntityMap2 = new ConcurrentHashMap<>();

    @Override
    public void init() {
        saveEntityMap = survivorEntityMap1;
        landEntity();
    }

    public <T extends SaveObject> void save(SaveAble saveAbleEntity) {
        long start = System.currentTimeMillis();
        synchronized (locker) {
            saveEntityMap.put(saveAbleEntity.getId(), new SaveCache(saveAbleEntity, serializationProcessor.writeByte(saveAbleEntity), false));
        }
        long end = System.currentTimeMillis();
        if (end - start > 500) {
            log.warn("save object take more than 500 milliseconds, {} milliseconds used type_{} Id_{}, entity_{}", end - start, saveAbleEntity.getType(), saveAbleEntity.getId(), saveAbleEntity);
        }
    }

    private void landEntity() {
        Span span = ZipkinUtil.getTracing().tracer().nextSpan();
        span.name("landEntity");

        Map<String, SaveCache> lastEntityMap;
        long start = System.currentTimeMillis();
        // 切换队列, 需要加锁
        synchronized (locker) {
            lastEntityMap = saveEntityMap;
            saveEntityMap = switchToOtherMap();
        }
        List<Homo<Boolean>> storagePromiseList = new ArrayList<>();
        for (SaveCache saveCache : lastEntityMap.values()) {
            SaveObject saveObject = saveCache.saveObject;
            Homo<Boolean> savePromise = storage.save(getServerInfo().appId, getServerInfo().regionId, saveObject.getLogicType(), saveObject.getOwnerId(), ByteStorage.DEFAULT_DATA_KEY, saveCache.getData())
                    .consumerValue(ret -> {
                        if (ret) {
                            saveCache.setSave(true);
                        }
                        log.info("byteStorage save ret {} type {} id {}", ret, saveObject.getLogicType(), saveObject.getOwnerId());
                    });
            storagePromiseList.add(savePromise);
        }
        Homo.when(storagePromiseList)
                .consumerValue(ret -> {
                    //清空
                    getLandEntity().clear();
                    timerMgr.once("landEntity", CallQueueMgr.getInstance().getQueue(CallQueueMgr.frame_queue_id), this::landEntity, abilityProperties.getIntervalSecondMillis());
                    log.trace("landEntity finish ");
                }).catchError(throwable -> {
                    log.error("landEntity error ", throwable);
                    Map<String, SaveCache> landEntity = getLandEntity();
                    landEntity.entrySet().removeIf(item -> item.getValue().isSave);
                    timerMgr.once("landEntity", CallQueueMgr.getInstance().getQueue(CallQueueMgr.frame_queue_id), this::landEntity, abilityProperties.getIntervalSecondMillis());
                })
                .finallySignal(ret -> {
                    long end = System.currentTimeMillis();
                    if (end - start > 500) {
                        log.warn("landEntity take more than 500 milliseconds, {} milliseconds used, storagePromiseList {}", end - start, storagePromiseList.size());
                    }
                })
                .start();

    }

    private Map<String, SaveCache> switchToOtherMap() {
        return saveEntityMap == survivorEntityMap1 ? survivorEntityMap2 : survivorEntityMap1;
    }

    private Map<String, SaveCache> getLandEntity() {
        return switchToOtherMap();
    }

    public <T extends AbilityEntity> Homo<T> loadEntity(Class<T> clazz, String id) {
        log.info("loadEntity start clazz {} id {}", clazz, id);
        if (!SaveAble.class.isAssignableFrom(clazz)) {
            return Homo.error(new Exception(String.format("loadEntity error clazz is not saveAble entity clazz [%s] id = [%s] ", clazz, id)));
        }
        EntityType entityType = HomoAnnotationUtil.findAnnotation(clazz, EntityType.class);
        if (entityType == null) {
            return Homo.error(new Exception(String.format("loadEntity error entityType is null class [%s] id = [%s] ", clazz, id)));
        }
        String type = entityType.type();
        return load(type, id, (Class<SaveObject>) (Class<?>) clazz)
                .nextDo(ret -> {
                    log.info("loadEntity end clazz {} id {} ret {}", clazz, id, ret);
                    if (ret == null) {
                        return Homo.result(null);
                    } else {
                        return Homo.result((T) ret);
                    }
                });
    }

    public <E extends SaveObject> Homo<E> load(String logicType, String id, Class<E> zz) {
        return Homo.warp(() -> locker.lockCallable(id, () -> {
                    // 先取缓存中的值
                    E cacheData = (E) getInCache(logicType, id);
                    if (cacheData != null) {
                        // 如果缓存里面有就直接返回
                        return Homo.result(cacheData);
                    } else {
                        // 否则就从storage中加载
                        return loadFromStorage(logicType, id, zz);
                    }
                })
        );
    }

    <E extends SaveObject> Homo<E> loadFromStorage(String logicType, String id, Class<E> zz) {
        log.info("loadFromStorage load start logicType {} id {} clazz {}", logicType, id, zz);
        E cached = (E) getInCache(logicType, id);
        if (cached != null) {
            // 如果有了就直接取缓存中的值
            return Homo.result(cached).consumerValue(ret -> {
                log.info("loadFromStorage load form cache  logicType {} id {} clazz {} ret {}", logicType, id, zz, ret);
            });
        }
        return storage.load(logicType, id, zz)
                .ifEmptyThen(Homo.result(null))
                .consumerValue(ret -> {
                    log.info("loadFromStorage load from persistent () logicType {} id {} clazz {} ret {}", logicType, id, zz, ret);
                });
    }

    String getKey(String type, String id) {
        return type + id;
    }

    synchronized private SaveObject getInCache(String logicType, String id) {
        SaveCache cache = getLandEntity().get(getKey(logicType, id));
        if (cache != null) {
            return cache.saveObject;
        }
        cache = saveEntityMap.get(getKey(logicType, id));
        if (cache != null) {
            return cache.saveObject;
        }
        return null;
    }

    public boolean allEntityLanded() {
        return survivorEntityMap1.size() == 0 && survivorEntityMap2.size() == 0;
    }
}
