package com.homo.core.mq.consumer;

import com.homo.core.facade.mq.MQCodeC;
import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.consumer.*;
import com.homo.core.mq.base.CodecRegister;
import com.homo.core.mq.base.MQDriverFactoryProvider;
import com.homo.core.mq.consumer.route.RouterMgr;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 消息队列消费者实现
 */
@Slf4j
public class MQConsumerImpl implements MQConsumer {
    /**
     * 底层驱动
     */
    protected MQConsumerDriver driver;
    /**
     * 编码解码器集合
     */
    public CodecRegister<byte[]> codecRegister;
    /**
     * 缓存真实的Topic，避免重复创建
     */
    final Map<String, String> originTorealTopicCacheMap;
    protected Set<ErrorListener> listeners;
    protected MQConsumerConfig consumerConfig;
    protected RouterMgr routerMgr;
    protected volatile Status status;

    public MQConsumerImpl(MQConsumerConfig consumerConfig) {
        MQType mqType = consumerConfig.getType();
        MQConsumerDriverFactory driverFactory = MQDriverFactoryProvider.getConsumerDriverFactory(mqType);
        if (driverFactory == null) {
            throw new RuntimeException(String.format("no consumer driver implementation found for MQType %s !", mqType));
        } else {
            log.info(String.format("find the consumer driver implementation for MQType %s .", mqType));
        }
        this.driver = driverFactory.create(consumerConfig.getGroupId());
        this.routerMgr = new RouterMgr();
        this.codecRegister = new CodecRegister<>();
        this.originTorealTopicCacheMap = new HashMap<>();
        this.status = Status.INIT;
        this.listeners = new HashSet<>();
        this.consumerConfig = consumerConfig;
    }

    @Override
    public @NotNull MQType getType() {
        return driver.getType();
    }

    public @NotNull String getRealTopic(@NotNull String originTopic) {
        return originTorealTopicCacheMap.computeIfAbsent(originTopic, (_topic) -> consumerConfig.getTopicResolveStrategy().getRealTopic(originTopic, consumerConfig.getAppId(), consumerConfig.getRegionId()));
    }

    @Override
    public <T extends Serializable> void addReceiver(@NotNull String originTopic, @NotNull ReceiverSink<T> sink) throws Exception {
        if (status != Status.INIT) {
            throw new RuntimeException("必须要在Consumer未start之前调用receive");
        }
        String realTopic = getRealTopic(originTopic);
        routerMgr.register(realTopic, sink);
        if (!driver.haveSubscribe(realTopic)) {
            driver.subscribe(realTopic, receiverSink);
        }
    }

    @Override
    public void addReceiver(@NotNull SinkHandler sinkHandler) throws Exception {
        if (status != Status.INIT) {
            throw new RuntimeException("必须要在Consumer未start之前调用receive");
        }
        Class<?> clazz = sinkHandler.getClass();
        Method[] declaredMethods = clazz.getDeclaredMethods();

        for (Method declaredMethod : declaredMethods) {
            SinkFunc sinkFunc = declaredMethod.getAnnotation(SinkFunc.class);
            if (sinkFunc != null) {
                for (String topic : sinkFunc.topics()) {
                    routerMgr.register(getRealTopic(topic), sinkHandler, declaredMethod);
                }
            }

        }
    }

    private ReceiverSink<byte[]> receiverSink = new ReceiverSink<byte[]>() {
        @Override
        public void onSink(String topic, byte[] bytes, ConsumerCallback callback) {
            Serializable message = null;
            try {
                MQCodeC<Serializable, byte[]> codec = codecRegister.getCodec(topic);
                message = codec.decode(bytes);
                log.debug("MQConsumer receiverSink topic {} message {}", topic, message);
                routerMgr.topicRouter(topic, message, callback);
            } catch (Throwable throwable) {
                callback.confirm();
                listeners.forEach(item -> item.onError(topic, bytes, throwable));
            }
        }
    };

    public <T extends java.io.Serializable> void registerCodec(@NotNull String originTopic, @NotNull MQCodeC<T, byte[]> codec) {
        String realTopic = this.getRealTopic(originTopic);
        codecRegister.setCodec(realTopic, codec);
    }

    @Override
    public <T extends Serializable> void registerGlobalCodec(@NotNull MQCodeC<T, byte[]> codec) {
        codecRegister.setDefaultCodec(codec);
    }

    @Override
    public void addErrorListener(ErrorListener listener) {
        listeners.add(listener);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * 启动消费者，开始从消息队列中消费消息。只允许调用一次。
     */
    @Override
    public void start() {
        if (status == Status.INIT) {
            status = Status.STARTING;
            driver.start();
            status = Status.RUNNING;
        } else {
            log.error("current consumer status is {}, not allow call start", status);
        }
    }

    /**
     * 停止消费者，不再从消息队列中消费消息。只允许调用一次。
     */
    @Override
    public void stop() {
        if (status == Status.RUNNING) {
            status = Status.STOPPING;
            driver.stop();
            status = Status.STOPPED;
        } else {
            log.error("current consumer status is {}, not allow call stop", status);
        }
    }
}
