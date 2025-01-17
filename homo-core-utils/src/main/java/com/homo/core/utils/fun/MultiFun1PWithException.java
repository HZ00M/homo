package com.homo.core.utils.fun;

@FunctionalInterface
public interface MultiFun1PWithException<T,R>{
    R apply(T t,Object... o) throws Exception;
}
