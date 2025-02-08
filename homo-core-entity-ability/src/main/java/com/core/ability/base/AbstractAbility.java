package com.core.ability.base;

import com.alibaba.fastjson.annotation.JSONField;
import com.homo.core.facade.ability.Ability;
import com.homo.core.facade.ability.AbilityEntity;

/**
 * 抽象能力
 */
public abstract class AbstractAbility implements Ability {
    protected AbilityEntity abilityEntity;
    @Override
    public void attach(AbilityEntity abilityEntity) {
        this.abilityEntity = abilityEntity;
        abilityEntity.setAbility(this);
        log.info("Ability attach to entity name {} type {} id {} ", this.getClass().getSimpleName(), abilityEntity.getType(), abilityEntity.getId());
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public  AbilityEntity getOwner() {
        return abilityEntity;
    }
}
