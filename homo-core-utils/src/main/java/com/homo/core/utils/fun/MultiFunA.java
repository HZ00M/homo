package com.homo.core.utils.fun;

@FunctionalInterface
public interface MultiFunA<T,R>{
    R apply(T t,Object... o) throws Exception;
}
