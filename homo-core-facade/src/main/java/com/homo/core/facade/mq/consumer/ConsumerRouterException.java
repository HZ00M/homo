package com.homo.core.facade.mq.consumer;

public class ConsumerRouterException extends RuntimeException{
    public String topic;
    public Object message;
    public <T> ConsumerRouterException(String topic,T message,Throwable cause){
        super(cause);
        this.topic = topic;
        this.message = message;
    }
}
