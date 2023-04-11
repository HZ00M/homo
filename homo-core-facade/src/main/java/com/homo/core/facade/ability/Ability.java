package com.homo.core.facade.ability;

/**
 * 能力
 */
public interface Ability {
    /**
     * 关联对象
     * @param abilityObject
     */
    void attach(String abilityName, AbilityObject abilityObject);

    default void attach(AbilityObject abilityObject) {
        attach(abilityObject.getClass().getSimpleName(), abilityObject);
    }

    /**
     * 取消关联
     */
    void unAttach();

    /**
     * 获取关联的对象
     * @return
     */
    AbilityObject getOwner();

}
