package com.homo.core.facade.storege;

import java.io.Serializable;

/**
 * 对象保存接口
 **/
public interface SaveObject extends Serializable {
    /**
     * 获取logicalType
     * @return logicalType
     */
    String getLogicType();

    /**
     * 获取OwnerId
     * @return OwnerId
     */
    String getOwnerId();
}
