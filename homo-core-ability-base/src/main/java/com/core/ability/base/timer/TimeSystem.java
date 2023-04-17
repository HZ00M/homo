package com.core.ability.base.timer;

import com.homo.core.facade.ability.AbilityObjectMgr;
import com.homo.core.facade.ability.AbilitySystem;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class TimeSystem implements AbilitySystem {
    @Override
    public void init(AbilityObjectMgr abilityObjectMgr) {
        abilityObjectMgr.registerAddProcess(TimeAble.class, TimeAbility::new);
    }
}
