package com.homo.core.facade.mq.producer;

import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.strategy.TopicResolveStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
/**
 * 生产者配置
 */
public class MQProducerConfig {
    /**
     * 队列类型
     */
    MQType type;
    /**
     * appId
     */
    String appId;
    /**
     * 区服id
     */
    String regionId;
    /**
     * topic解析策略
     */
    TopicResolveStrategy topicResolveStrategy;
}
