package com.homo.core.facade.rpc;

import brave.Span;
import io.homo.proto.client.ParameterMsg;

public interface RpcContent<P,R> {
    RpcContentType getType();

    P getData();
    void setData(P data);

    Object[] unSerializeParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset, Integer podId , ParameterMsg parameterMsg);

    R serializeParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset);

    Span getSpan();

    void setSpan(Span span);
}
