package com.core.ability.base.timer;

import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.facade.ability.AbilitySystem;
import com.homo.core.facade.ability.TimeAble;
import lombok.extern.slf4j.Slf4j;

/**
 * 对象定时器系统
 */
@Slf4j
public class TimeSystem implements AbilitySystem {
    @Override
    public void init(AbilityEntityMgr abilityEntityMgr) {
        abilityEntityMgr.registerAddProcess(TimeAble.class, TimeAbility::new);
    }
}
