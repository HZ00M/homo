package com.homo.core.mq.consumer.route;

import com.homo.core.facade.mq.consumer.ConsumerCallback;
import com.homo.core.facade.mq.consumer.ReceiverSink;
import com.homo.core.facade.mq.consumer.SinkHandler;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 路由信息类。每一个实例对应一个处理函数
 */
@ToString
public class RouteTarget {
    private ReceiverSink receiverSink;
    private SinkHandler handler;
    private Method func;
    @Getter
    private Class<?> messageClazz;
    private final TargetType targetType;

    public RouteTarget(ReceiverSink receiverSink) {
        this.receiverSink = receiverSink;
        this.targetType = TargetType.SINK;
    }

    public RouteTarget(SinkHandler handler, Method func) {
        this.handler = handler;
        this.func = func;
        this.messageClazz = func.getParameterTypes()[1];
        this.targetType = TargetType.HANDLER;
    }

    public void invoke(String realTopic, Serializable message, ConsumerCallback callback) throws Exception{
        if (targetType.equals(TargetType.SINK)) {
            receiverSink.onSink(realTopic, message, callback);
        } else {
            func.invoke(handler, realTopic, message, callback);
        }
    }

    public enum TargetType {
        SINK,
        HANDLER
    }
}
