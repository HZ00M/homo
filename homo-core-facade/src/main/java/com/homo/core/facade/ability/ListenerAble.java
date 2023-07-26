package com.homo.core.facade.ability;

import com.homo.core.facade.ability.AbilityAble;

/**
 * 为对象提供事件监听能力
 */
public interface ListenerAble extends AbilityAble {
    void notify(String notifyType, Object...objects);
}
