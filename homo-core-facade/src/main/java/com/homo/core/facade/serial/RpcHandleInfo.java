package com.homo.core.facade.serial;

import reactor.util.function.Tuple2;

public interface RpcHandleInfo<INVOKE,RETURN> {

    MethodDispatchInfo getMethodDispatchInfo();

    INVOKE unSerializeParamForInvoke(String funName,RpcContent rpcContent,Integer pod,Object parameterMsg);

    RETURN serializeParamForReturn(RpcContent.RpcContentType contentType,String funKey,Tuple2<String,Object[]> result);
}
