package com.homo.core.facade.ability;

/**
 * 为对象提供事件监听能力
 */
public interface ListenerAble extends AbilityAble {
    void notify(String notifyType, Object...objects);
}
