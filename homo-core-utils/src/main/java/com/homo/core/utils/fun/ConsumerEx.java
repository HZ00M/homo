package com.homo.core.utils.fun;

@FunctionalInterface
public interface ConsumerEx<T> {

    void accept(T t) throws Exception;

}
