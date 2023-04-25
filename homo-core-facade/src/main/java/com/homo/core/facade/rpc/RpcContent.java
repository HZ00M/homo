package com.homo.core.facade.rpc;

public interface RpcContent<T> {
    RpcContentType getType();

    T getData();

    void setData(T data);

    Object[] unSerializeParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset);

    byte[][] serializeParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset);
}
