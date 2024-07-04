package com.homo.core.facade.mq.consumer;

public interface ErrorListener {
    void onError(String topic,byte[] bytes,Throwable throwable);
}
