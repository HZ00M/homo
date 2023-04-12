package com.core.ability.base.storage;

import com.homo.core.configurable.ability.AbilityProperties;
import com.homo.core.facade.ability.AbilityObjectMgr;
import com.homo.core.facade.ability.AbilitySystem;
import com.homo.core.root.storage.ByteStorage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class StorageSystem implements AbilitySystem {
    final Object lockForSaveEntityMap = new Object();
    @Autowired
    AbilityProperties abilityProperties;

    @Autowired
    ByteStorage byteStorage;
    @Override
    public void init(AbilityObjectMgr abilityObjectMgr) {
        abilityObjectMgr.registerAddProcess(SaveAble.class,StorageAbility::new);
    }
}
