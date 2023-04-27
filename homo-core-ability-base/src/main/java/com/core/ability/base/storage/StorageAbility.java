package com.core.ability.base.storage;

import com.core.ability.base.AbstractAbility;
import com.core.ability.base.notify.WatchAbility;
import com.core.ability.base.timer.TimeAbility;
import com.homo.core.configurable.ability.AbilityProperties;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerMgr;
import com.homo.core.utils.concurrent.schedule.HomoTimerTask;
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
    long updateTime;

    String SAVE_EVENT = "save";

    public void save() {
        storageSystem.save((SaveAble) getOwner());
        WatchAbility watchAbility = getOwner().getAbility(WatchAbility.class);
        watchAbility.notify(SAVE_EVENT, updateTime);
    }

    public StorageAbility(AbilityEntity abilityEntity) {
        AbilityProperties abilityProperties = GetBeanUtil.getBean(AbilityProperties.class);
        StorageTime storageTime = HomoAnnotationUtil.findAnnotation(abilityEntity.getClass(), StorageTime.class);
        if (storageTime != null) {
            this.saveTime = storageTime.value();
        }else {
            this.saveTime = abilityProperties.getSaveTimeSecondMills();
        }
        CacheTime cacheTime = HomoAnnotationUtil.findAnnotation(abilityEntity.getClass(), CacheTime.class);
        if (cacheTime != null) {
            this.cacheTime = cacheTime.value();
        }else {
            this.cacheTime = abilityProperties.getCacheTimeSecondMillis();
        }
        updateTime = System.currentTimeMillis();
        attach(abilityEntity);
    }

    @Override
    public Homo<Void> promiseAfterInitAttach(AbilityEntity abilityEntity){
        return super.promiseAfterInitAttach(abilityEntity)
                .consumerValue(ret->{
                    log.info("attach type {} id {}", abilityEntity.getType(), abilityEntity.getId());
                    storageSystem = GetBeanUtil.getBean(StorageSystem.class);
                    // 配置了定时保持或者缓存时间
                    // 如果定时保存，就按照定时保存的时间起定时器，如果定时保存的时间比缓存时间大，那么可能会多缓存一会
                    long timerTimer = saveTime > 0 ? saveTime : cacheTime;
                    CallQueueMgr.getInstance().task(() -> {
                        HomoTimerTask task = new HomoTimerTask().setOnCancelConsumer(needCancelTask -> {
                            if (cacheTime != null && System.currentTimeMillis() - updateTime >= cacheTime) {
                                // 配置了缓存时间，且过了缓存时间
                                if (System.currentTimeMillis() - updateTime > cacheTime) {
                                    // 缓存时间到了，取消定时器，清除缓存
                                    needCancelTask.cancel();
                                    // 超过了时间就释放掉
                                    log.info("save and destroy [{}] [{}], currentTimeMillis:[{}] - updateTime:[{}] = [{}] > cacheTime:[{}]", getOwner().getType(), getOwner().getId(), System.currentTimeMillis(), updateTime, System.currentTimeMillis() - updateTime, cacheTime);
                                    getOwner().promiseDestroy().start();
                                }
                            } else {
                                if (updateTime > lastSaveTime) {
                                    log.info("save [{}] [{}]", getOwner().getType(), getOwner().getId());
                                    save();
                                }
                            }
                        });
                        getOwner().getAbility(TimeAbility.class).newTimer(task, timerTimer, timerTimer, HomoTimerMgr.UNLESS_TIMES);
                    }, abilityEntity.getQueueId());
                });
    }

    @Override
    public void unAttach(AbilityEntity abilityEntity) {
        log.info(" unAttach Entity type_{} id_{}", getOwner().getType(), getOwner().getId());
        save();
    }

}
