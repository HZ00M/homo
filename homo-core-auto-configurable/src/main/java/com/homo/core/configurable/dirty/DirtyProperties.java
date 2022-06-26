package com.homo.core.configurable.dirty;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class DirtyProperties {
    @Value("${homo.dirty.tableNum:4}")
    private int tableNum;

    @Value("${homo.dirty.table.prefix:dirtyKey}")
    private String tablePrefix;

    @Value("${homo.dirty.lock.expireTime:1000}")
    private Integer lockExpireTime;

    @Value("${homo.dirty.snapshot.suffix:saving}")
    private String snapshotSuffix;

    @Value("${homo.dirty.lock.error.suffix:error}")
    private String errorSuffix;

    @Value("${homo.dirty.landing.batchNum:1000}")
    private String batchNum;

    @Value("${homo.dirty.landing.delayTime:10}")
    private long delayTime;
}
