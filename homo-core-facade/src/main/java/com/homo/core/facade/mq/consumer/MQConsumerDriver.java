package com.homo.core.facade.mq.consumer;

import com.homo.core.facade.mq.MQSupport;
import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.consumer.ReceiverSink;
import org.jetbrains.annotations.NotNull;

/**
 * 驱动层接收到的Topic就是真实的Topic。
 * 驱动层不处理Topic包装，只负责向队列服务器发送字节消息
 */
public interface MQConsumerDriver extends MQSupport {


    /**
     * 消费一个Topic，收到数据后回调sink下沉
     * @param topic
     * @param sink
     */
    void subscribe(final String topic, ReceiverSink<byte[]> sink);

    /**
     * 判断驱动是否已订阅指定的topic，
     * 在驱动层，应该一个topic只启动一个线程订阅一次
     * @param topic
     * @return
     */
    boolean haveSubscribe(final String topic);

    /**
     * 开启驱动所有工作线程，开始消费消息。只允许调用一次
     */
    void start();

    /**
     * 停止驱动所有工作线程。只允许调用一次
     */
    void stop();

}
