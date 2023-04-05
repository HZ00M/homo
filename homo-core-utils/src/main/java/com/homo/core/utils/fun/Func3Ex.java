package com.homo.core.utils.fun;

@FunctionalInterface
public interface Func3Ex<T, T1,T2, R> {
    R apply(T t, T1 t1, T2 t2) throws Exception;
}
