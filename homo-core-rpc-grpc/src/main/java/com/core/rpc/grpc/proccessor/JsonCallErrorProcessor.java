package com.core.rpc.grpc.proccessor;

import io.homo.proto.rpc.*;

import java.util.function.BiFunction;

public enum JsonCallErrorProcessor implements BiFunction<JsonReq,Throwable, JsonRes> {
    Default{
        @Override
        public JsonRes apply(JsonReq req, Throwable throwable) {
            JsonRes.Builder builder = JsonRes.newBuilder().setMsgId(DEFAULT_ERROR);
            return builder.build();
        }
    }
    ;

    public static String DEFAULT_ERROR = "MSG_ERROR";

    public static JsonRes processError(JsonReq req, Throwable e){
        return Default.apply(req,e);
    }
}
