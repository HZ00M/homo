package com.core.ability.base.storage;

import com.core.ability.base.AbstractAbility;
import com.core.ability.base.notify.WatchAbility;
import com.core.ability.base.timer.TimeAbility;
import com.homo.core.configurable.ability.AbilityProperties;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.facade.ability.CacheTime;
import com.homo.core.facade.ability.SaveAble;
import com.homo.core.facade.ability.StorageTime;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import com.homo.core.utils.spring.GetBeanUtil;

/**
 * 对象保存能力实现
 */

public class StorageAbility extends AbstractAbility {
    StorageSystem storageSystem;
    Long saveTime;
    Long cacheTime;
    long lastSaveTime;
    long currentGetTime;

    String SAVE_EVENT = "save";

    public void save() {
        storageSystem.save((SaveAble) getOwner());
        WatchAbility watchAbility = getOwner().getAbility(WatchAbility.class);
        if (watchAbility != null) {
            watchAbility.notify(SAVE_EVENT, currentGetTime);
        }
    }

    public StorageAbility(AbilityEntity abilityEntity) {
        AbilityProperties abilityProperties = GetBeanUtil.getBean(AbilityProperties.class);
        StorageTime storageTime = HomoAnnotationUtil.findAnnotation(abilityEntity.getClass(), StorageTime.class);
        if (storageTime != null) {
            this.saveTime = storageTime.value();
        } else {
            this.saveTime = abilityProperties.getSaveTimeSecondMills();
        }
        CacheTime cacheTime = HomoAnnotationUtil.findAnnotation(abilityEntity.getClass(), CacheTime.class);
        if (cacheTime != null) {
            this.cacheTime = cacheTime.value();
        } else {
            this.cacheTime = abilityProperties.getCacheTimeSecondMillis();
        }
        currentGetTime = System.currentTimeMillis();
        attach(abilityEntity);
    }

    @Override
    public Homo<Void> promiseAfterInitAttach(AbilityEntity abilityEntity) {
        storageSystem = GetBeanUtil.getBean(StorageSystem.class);
        return super.promiseAfterInitAttach(abilityEntity)
                .consumerValue((ret) -> {

                    // 配置了定时保持或者缓存时间
                    // 如果定时保存，就按照定时保存的时间起定时器，如果定时保存的时间比缓存时间大，那么可能会多缓存一会
                    long updateStep = saveTime > 0 ? saveTime : cacheTime;
                    log.info("TimeAbility attach type {} id {} updateStep {}", abilityEntity.getType(), abilityEntity.getId(), updateStep);
                    CallQueueMgr.getInstance().task(() -> {
                        getOwner().getAbility(TimeAbility.class).newTimer(getOwner().getId(), () -> {
                            long diffTime = System.currentTimeMillis() - currentGetTime;
                            if (cacheTime != null && diffTime > cacheTime) {
                                // 配置了缓存时间，且过了缓存时间
                                // 缓存时间到了，取消定时器，清除缓存
                                getOwner().getAbility(TimeAbility.class).cancel(getOwner().getId());
                                // 超过了时间就释放掉
                                log.info("TimeAbility destroy [{}] [{}], currentTimeMillis:[{}] - updateTime:[{}]  diffTime [{}] > cacheTime:[{}]", getOwner().getType(), getOwner().getId(), System.currentTimeMillis(), currentGetTime, diffTime, cacheTime);
                                getOwner().promiseDestroy().start();
                            } else {
                                if (currentGetTime > lastSaveTime) {
                                    log.info("TimeAbility save [{}] [{}] updateTime:[{}] lastSaveTime:[{}]  diffTime [{}]", getOwner().getType(), getOwner().getId(), currentGetTime, lastSaveTime, diffTime);
                                    save();
                                    lastSaveTime = currentGetTime;
                                } else {
                                    log.info("TimeAbility save doNothing [{}] [{}] updateTime:[{}] lastSaveTime:[{}]  diffTime [{}]", getOwner().getType(), getOwner().getId(), currentGetTime, lastSaveTime, diffTime);
                                }
                            }
                        }, updateStep, updateStep, HomoTimerMgr.UNLESS_TIMES);
                    }, abilityEntity.getQueueId());
                });
    }

    @Override
    public void unAttach(AbilityEntity abilityEntity) {
        log.info(" unAttach Entity type_{} id_{}", getOwner().getType(), getOwner().getId());
        save();
    }

    public void currentGet() {
        long oldTime = currentGetTime;
        currentGetTime = System.currentTimeMillis();
        log.debug("updateTime type {} id {} oldTime {} newTime {} ", getOwner().getType(), getOwner().getId(), oldTime, currentGetTime);

    }
}
