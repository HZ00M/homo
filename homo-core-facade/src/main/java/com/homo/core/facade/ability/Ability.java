package com.homo.core.facade.ability;

import com.homo.core.utils.rector.Homo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 能力
 */
public interface Ability {
    Logger log = LoggerFactory.getLogger(Ability.class);

//    void attach(String abilityName, AbilityEntity abilityEntity);

//    default void attach(AbilityEntity abilityEntity) {
//        attach(abilityEntity.getClass().getSimpleName(), abilityEntity);
//    }

    /**
     * 关联对象
     *
     * @param abilityEntity
     */
    void attach(AbilityEntity abilityEntity);

    default void afterAttach(AbilityEntity abilityEntity) {

    }

    /**
     * 取消关联
     */
    void unAttach(AbilityEntity abilityEntity);

    default void afterUnAttach(AbilityEntity abilityEntity) {

    }

    default Homo<Void> promiseAfterInitAttach(AbilityEntity abilityEntity) {
        return Homo.warp(
                sink -> {
                    attach(abilityEntity);
                    log.info("{} promiseAfterInitAttach->attach(abilityEntity)->done type {} id {} ", this.getClass().getSimpleName(), abilityEntity.getType(), abilityEntity.getId());
                    afterAttach(abilityEntity);
                    sink.success();
                }
        );
    }

    default Homo<Void> promiseBeforeDestroyUnAttach(AbilityEntity abilityEntity) {
        return Homo.warp(
                sink -> {
                    unAttach(abilityEntity);
                    log.info("{} promiseBeforeDestroyUnAttach->unAttach(abilityEntity)->done type {} id {} ", this.getClass().getSimpleName(), getOwner().getType(), getOwner().getId());
                    afterUnAttach(abilityEntity);
                    sink.success();
                }
        );
    }


    /**
     * 获取关联的对象
     *
     * @return
     */
    AbilityEntity getOwner();

}
