package com.homo.core.facade.rpc;

import brave.Span;
import io.homo.proto.client.ParameterMsg;


public interface RpcContent<P,R> {
    String getMsgId();

     void setMsgId(String id);

    RpcContentType getType();

    P getParam();

    void setParam(P data);

    public void setReturn(R returnData);

    public R getReturn();

    Object[] unSerializeToActualParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset, Integer podId , ParameterMsg parameterMsg);

    P serializeRawParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset);

    R serializeReturn(Object param, SerializeInfo returnSerializeInfo);

    void setReturnType(Class<?> returnType);

    Span getSpan();

    void setSpan(Span span);

    Object unSerializeReturnValue(SerializeInfo returnSerializeInfo);
}
