package com.core.ability.base.timer;

import com.homo.core.facade.ability.AbilityEntityMgr;
import com.homo.core.facade.ability.AbilitySystem;
import com.homo.core.facade.ability.TimeAble;
import lombok.extern.log4j.Log4j2;

/**
 * 对象定时器系统
 */
@Log4j2
public class TimeSystem implements AbilitySystem {
    @Override
    public void init(AbilityEntityMgr abilityEntityMgr) {
        abilityEntityMgr.registerAddProcess(TimeAble.class, TimeAbility::new);
    }
}
