package com.homo.core.facade.mq.producer;

public interface ProducerCallback {

    /**
     * 生产者发送回调接口
     * @param ok
     * @param throwable
     */
    void onCompletion(boolean ok,Throwable throwable);
}
