package com.homo.core.facade.rpc;

import io.homo.proto.client.ParameterMsg;

public interface RpcContent<T> {
    RpcContentType getType();

    T getData();
    void setData(T data);

    Object[] unSerializeParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset, Integer podId , ParameterMsg parameterMsg);

    T serializeParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset);
}
