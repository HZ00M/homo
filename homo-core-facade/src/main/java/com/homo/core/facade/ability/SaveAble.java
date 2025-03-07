package com.homo.core.facade.ability;

import com.homo.core.facade.storege.SaveObject;

/**
 * 为对象添加自动保存能力
 */
public interface SaveAble extends AbilityAble, SaveObject, Entity {

    /**
     * 获取logicalType
     *
     * @return logicalType
     */
    default String getLogicType() {
        return getType();
    }

    /**
     * 获取OwnerId
     *
     * @return OwnerId
     */
    default String getOwnerId() {
        return getId();
    }
}
