package com.core.ability.base;

import com.core.ability.base.call.CallAble;
import com.core.ability.base.timer.TimeAble;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.homo.core.facade.ability.Ability;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.facade.ability.EntityType;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import com.homo.core.utils.spring.GetBeanUtil;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象能力实体
 */
@Log4j2
@ToString
public class AbstractAbilityEntity implements AbilityEntity, TimeAble, CallAble {
    static Map<Class<?>, String> entityClazzToEntityTypeMap = new ConcurrentHashMap<>();

    protected String id;
    protected Integer queueId;

    Map<String, Ability> abilityMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Ability> T getAbility(String abilityName) {

        return (T) abilityMap.get(abilityName);
    }

    @Override
    public void setAbility(String abilityName, Ability ability) {
        abilityMap.put(abilityName, ability);
    }

    @Override
    public Homo<Void> promiseInit() {
        log.info("promiseInit start type {} id {}", getType(), id);
        if (getType() == null) {
            log.error("promiseInit init error, type is null");
        }
        if (getId() == null) {
            log.error("promiseInit init error, id is null");
        }
        queueId = CallQueueMgr.getInstance().choiceQueueIdBySeed(id.hashCode());
        return Homo.result(this)
                .nextDo(entity -> {
                    if (!GetBeanUtil.getBean(AbilityEntityMgr.class).add(entity)) {
                        return Homo.error(
                                new Exception(String.format("entity create error! type_%s id_%s already exist!", getType(), getId())));
                    }
                    return Homo.result(entity);
                })
                // 切换到entity对应的线程 todo 是否有必要?
                .switchThread(queueId)
                .nextDo(entity -> {
                    List<Homo<Void>> afterInitAbility = new ArrayList<>(abilityMap.size());
                    for (Ability ability : abilityMap.values()) {
                        afterInitAbility.add(ability.promiseAfterInitAttach(entity));
                    }
                    return Homo.when(afterInitAbility).justThen(this::afterPromiseInit);
                });

    }

    protected Homo<Void> afterPromiseInit() {
        return Homo.result(null);
    }

    @Override
    public Homo<Void> promiseDestroy() {
        return Homo.result(this)
                .switchThread(queueId)
                .nextDo(entity -> {
                    List<Homo<Void>> beforeAfterDestroyAbility = new ArrayList<>(abilityMap.size());
                    for (Ability ability : abilityMap.values()) {
                        beforeAfterDestroyAbility.add(ability.promiseBeforeDestroyUnAttach(entity));
                    }
                    return Homo.when(beforeAfterDestroyAbility)
                            .justThen(() -> {
                                this.abilityMap.clear();
                                AbstractAbilityEntity remove = GetBeanUtil.getBean(AbilityEntityMgr.class).remove(entity);
                                log.info("promiseDestroy done remove entity {} {}", remove.getType(), remove.getId());
                                return afterPromiseDestroy();
                            });
                });
    }

    protected Homo<Void> afterPromiseDestroy() {
        return Homo.result(null);
    }

    //落地时需要忽略该get方法
    @JsonIgnore
    @Override
    public String getType() {
        String type = entityClazzToEntityTypeMap.get(getClass());//这里可能有并发问题，所以使用并发map
        if (type != null) {
            return type;
        }
        EntityType entityType = HomoAnnotationUtil.findAnnotation(getClass(), EntityType.class);
        if (entityType != null) {
            type = entityType.type();
            entityClazzToEntityTypeMap.put(getClass(), type);
            return type;
        }
        log.error("not found EntityType, object_{}", this);
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Integer getQueueId() {
        return queueId;
    }


    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }
}
