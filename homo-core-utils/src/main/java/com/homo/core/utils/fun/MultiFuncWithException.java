package com.homo.core.utils.fun;

@FunctionalInterface
public interface MultiFuncWithException<R>{
    R apply(Object... t) throws Exception;
}
