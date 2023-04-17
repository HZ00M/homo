package com.homo.core.facade.ability;

/**
 * ability系统
 * 在AbilityObjMgr初始化时会同步初始化
 */
public interface AbilitySystem {
    void init(AbilityEntityMgr abilityEntityMgr);
}
