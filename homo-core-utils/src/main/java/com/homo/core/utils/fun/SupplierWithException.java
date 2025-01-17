package com.homo.core.utils.fun;

@FunctionalInterface
public interface SupplierWithException<T> {
    T get() throws Exception;
}
