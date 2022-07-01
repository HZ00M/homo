package com.homo.core.utils.serial;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class HomoTypeReference<T> implements Comparable<HomoTypeReference<T>> {
    protected final Type type;

    protected HomoTypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() { return type; }

    @Override
    public int compareTo(HomoTypeReference<T> o) { return 0; }
}

