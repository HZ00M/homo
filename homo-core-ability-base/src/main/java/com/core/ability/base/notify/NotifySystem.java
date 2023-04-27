package com.core.ability.base.notify;

import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.facade.ability.AbilitySystem;
import lombok.extern.log4j.Log4j2;

/**
 * 通知系统
 */
@Log4j2
public class NotifySystem implements AbilitySystem {
    @Override
    public void init(AbilityEntityMgr abilityEntityMgr) {
        abilityEntityMgr.registerAddProcess(ListenerAble.class,WatchAbility::new);
    }
}
