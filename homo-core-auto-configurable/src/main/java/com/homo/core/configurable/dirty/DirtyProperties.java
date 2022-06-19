package com.homo.core.configurable.dirty;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class DirtyProperties {
    @Value("homo.dirty.tableNum")
    private int tableNum;

    @Value("homo.dirty.table.prefix:dirtyKey")
    private String tablePrefix;

    @Value("homo.dirty.lock.expireTime:1000")
    private Integer lockExpireTime;

    @Value("homo.dirty.snapshot.suffix:saving")
    private String snapshotSuffix;

    @Value("homo.dirty.lock.error.suffix:error")
    private String errorSuffix;

    @Value("homo.dirty.landing.batchNum:1000")
    private String batchNum;
}
