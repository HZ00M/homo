package com.homo.core.utils.fun;

import java.util.Objects;

@FunctionalInterface
public interface FuncWithException<T, R>{
    R apply(T t) throws Exception;

    static <T> FuncWithException<T, T> identity() {
        return t -> t;
    }

    default <V> FuncWithException<V, R> compose(FuncWithException<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> FuncWithException<T, V> andThen(FuncWithException<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }
}
