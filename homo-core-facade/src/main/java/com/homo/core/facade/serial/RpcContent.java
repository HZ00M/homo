package com.homo.core.facade.serial;

public interface RpcContent<T> {
    RpcContentType getType();

    T getData();

    void setData(T data);
}
