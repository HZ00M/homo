package com.homo.core.rpc.grpc.proccessor;

import io.homo.proto.rpc.StreamReq;
import io.homo.proto.rpc.StreamRes;

import java.util.function.BiFunction;

public enum StreamCallErrorProcessor implements BiFunction<StreamReq,Throwable, StreamRes> {
    Default{
        @Override
        public StreamRes apply(StreamReq req, Throwable throwable) {
            StreamRes.Builder builder = StreamRes.newBuilder().setMsgId(DEFAULT_ERROR);
            return builder.build();
        }
    }
    ;

    public static String DEFAULT_ERROR = "MSG_ERROR";

    public static StreamRes processError(StreamReq req, Throwable e){
        return Default.apply(req,e);
    }
}
