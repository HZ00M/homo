package com.homo.core.utils.fun;

@FunctionalInterface
public interface MultiFuncEx<R>{
    R apply(Object... t) throws Exception;
}
