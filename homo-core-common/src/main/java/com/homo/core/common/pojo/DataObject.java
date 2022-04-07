package com.homo.core.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class DataObject {
    private String primaryKey;

    private Integer logicType;

    private String ownerId;

    private String key;

    private byte[] value;

    private Long upVersion;

    private Integer isDel;

    private Long delTime;

    private String queryAllKey;
}
