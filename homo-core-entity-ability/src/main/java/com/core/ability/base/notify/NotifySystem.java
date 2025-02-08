package com.core.ability.base.notify;

import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.facade.ability.AbilitySystem;
import com.homo.core.facade.ability.ListenerAble;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知系统
 */
@Slf4j
public class NotifySystem implements AbilitySystem {
    @Override
    public void init(AbilityEntityMgr abilityEntityMgr) {
        abilityEntityMgr.registerAddProcess(ListenerAble.class,WatchAbility::new);
    }
}
