package com.homo.core.configurable.dirty;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class DirtyProperties {
    /**
     * 脏表数量，最佳实践是一个pod维护一个脏表
     */
    @Value("${homo.dirty.tableNum:1}")
    private int tableNum;

    /**
     * 落地表前缀名
     */
    @Value("${homo.dirty.table.prefix:dirtyKey}")
    private String tablePrefix;

    /**
     * 锁超时时间
     */
    @Value("${homo.dirty.lock.expireTime:1000}")
    private Integer lockExpireTime;

    /**
     * 快照表前缀名，快照表用于写时复制
     */
    @Value("${homo.dirty.snapshot.suffix:saving}")
    private String snapshotSuffix;

    /**
     * 异常表前缀，异常表用于存储落地失败的数据，供后期数据恢复
     */
    @Value("${homo.dirty.lock.error.suffix:error}")
    private String errorSuffix;

    /**
     * 批量落地数，默认一次落地1000条数据
     */
    @Value("${homo.dirty.landing.batchNum:1000}")
    private String batchNum;

    /**
     * 落地间隔时间
     */
    @Value("${homo.dirty.landing.delayTime:10}")
    private long delayTime;
}
