package com.homo.core.facade.ability;

/**
 * 能力
 */
public interface Ability {
    /**
     * 关联对象
     * @param abilityEntity
     */
    void attach(String abilityName, AbilityEntity abilityEntity);

    default void attach(AbilityEntity abilityEntity) {
        attach(abilityEntity.getClass().getSimpleName(), abilityEntity);
    }

    /**
     * 取消关联
     */
    void unAttach();

    /**
     * 获取关联的对象
     * @return
     */
    AbilityEntity getOwner();

}
