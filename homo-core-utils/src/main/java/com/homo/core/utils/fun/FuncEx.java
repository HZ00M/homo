package com.homo.core.utils.fun;

import java.util.Objects;

@FunctionalInterface
public interface FuncEx <T, R>{
    R apply(T t) throws Exception;

    static <T> FuncEx<T, T> identity() {
        return t -> t;
    }

    default <V> FuncEx<V, R> compose(FuncEx<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> FuncEx<T, V> andThen(FuncEx<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }
}
