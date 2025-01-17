package com.homo.core.utils.fun;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface Func2PWithException<T, U, R>{
    R apply(T t, U u) throws Exception;

    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> {
            try {
                return after.apply(apply(t, u));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
