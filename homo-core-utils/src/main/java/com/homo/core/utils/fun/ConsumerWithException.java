package com.homo.core.utils.fun;

@FunctionalInterface
public interface ConsumerWithException<T> {

    void accept(T t) throws Exception;

}
