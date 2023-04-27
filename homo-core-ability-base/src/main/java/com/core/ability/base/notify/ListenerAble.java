package com.core.ability.base.notify;

import com.homo.core.facade.ability.AbilityAble;

public interface ListenerAble extends AbilityAble {
    void notify(String notifyType, Object...objects);
}
