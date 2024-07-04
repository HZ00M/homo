package com.homo.core.facade.mq.consumer;

import java.io.Serializable;

/**
 * 业务层的消费接口 因为单方法的接口，可以是匿名类或lambda实现。
 * 如果是lambda实现，需要获取lambda参数信息，
 * 就必须让接口也实现Serializable，通过SerializedLambda 获取
 *
 * @param <T>
 */
@FunctionalInterface
public interface ReceiverSink<T extends Serializable> extends Serializable {

    void onSink(String topic, T message, ConsumerCallback callback);
}
