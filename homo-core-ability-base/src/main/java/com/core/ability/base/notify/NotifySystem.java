package com.core.ability.base.notify;

import com.homo.core.facade.ability.AbilityObjectMgr;
import com.homo.core.facade.ability.AbilitySystem;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NotifySystem implements AbilitySystem {
    @Override
    public void init(AbilityObjectMgr abilityObjectMgr) {
        abilityObjectMgr.registerAddProcess(ListenerAble.class,WatchAbility::new);
    }
}
