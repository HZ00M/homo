package com.homo.core.facade.ability;

/**
 * 实体
 */
public interface Entity {
    /**
     * 实体类型
     * @return
     */
    String getType();

    /**
     * 实体id
     * @return
     */
    String getId();

    void setId(String id);
}
