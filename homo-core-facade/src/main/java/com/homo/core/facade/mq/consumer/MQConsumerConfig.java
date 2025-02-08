package com.homo.core.facade.mq.consumer;

import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.strategy.TopicResolveStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
/**
 * 消费者配置
 */
public class MQConsumerConfig {
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
     * 消费者组
     */
    String groupId;
    /**
     * topic解析策略
     */
    TopicResolveStrategy topicResolveStrategy;
}
