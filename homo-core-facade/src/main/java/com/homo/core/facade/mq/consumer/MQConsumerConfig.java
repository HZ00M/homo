package com.homo.core.facade.mq.consumer;

import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.strategy.TopicResolveStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class MQConsumerConfig {
    MQType type;
    String appId;
    String regionId;
    String groupId;
    TopicResolveStrategy topicResolveStrategy;
}
