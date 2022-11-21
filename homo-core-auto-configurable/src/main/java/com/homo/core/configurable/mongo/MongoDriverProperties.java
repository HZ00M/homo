package com.homo.core.configurable.mongo;

import com.homo.core.configurable.NameSpaceConstant;
import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configurable
@Data
public class MongoDriverProperties {
    /**
     * 数据库连接
     */
    @Value("${homo.mongo.connString:mongodb://127.0.0.1:27017}")
    private String connString;
    /**
     * 数据库名
     */
    @Value("${homo.mongo.database:homo_storage}")
    private String database;
    /**
     * 连接池最小数量
     */
    @Value("${homo.mongo.minSize:1}")
    private Integer minSize;
    /**
     * 连接池最大数量
     */
    @Value("${homo.mongo.maxSize:100}")
    private Integer maxSize;
    @Value("${homo.mongo.maxWaitTime:100}")
    /**
     * 最大请求等待时间
     */
    private Long maxWaitTime;
    @Value("${homo.mongo.maxConnectionIdleTime:10000}")
    /**
     * 最长空闲等待时间
     */
    private Long maxConnectionIdleTime;
    /**
     * 最大连接存活时间
     */
    @Value("${homo.mongo.maxConnectionLifeTime:60000}")
    private Long maxConnectionLifeTime;
    /**
     * 重试写，默认开启（mongo默认配置，会有事务）
     */
    @Value("${homo.mongo.retryWrites:true}")
    private Boolean retryWrites;
    /**
     * 读偏好，有primary（只从主节点读），secondary（只从从节点读），primaryPreferred（优先从主节点读），secondaryPreferred（优先从从节点读），nearest（从最近的主机读）
     */
    @Value("${homo.mongo.readPreference:primary}")
    private String readPreference;
    /**
     * 写关注，有UNACKNOWLEDGED（不确认，最快，但最不安全），ACKNOWLEDGED（所有节点都确认，默认配置），JOURNALED（写入JOURNAL日志后确认），MAJORITY（大部分节点确认），W1（一个节点确认），W2，W3
     */
    @Value("${homo.mongo.writeConcern:ACKNOWLEDGED}")
    private String writeConcern;
}
