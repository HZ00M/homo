package com.homo.core.rpc.grpc.proccessor;

import com.google.protobuf.ByteString;
import io.homo.proto.rpc.Req;
import io.homo.proto.rpc.Res;

import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

public enum CallErrorProcessor implements BiFunction<Req,Throwable, Res> {
    Default{
        @Override
        public Res apply(Req req, Throwable throwable) {
            Res.Builder builder = Res.newBuilder().setMsgId(DEFAULT_ERROR)
                    .addMsgContent(ByteString.copyFrom(throwable.getMessage().getBytes(StandardCharsets.UTF_8)));
            return builder.build();
        }
    }
    ;

    public static String DEFAULT_ERROR = "MSG_ERROR";

    public static Res processError(Req req,Throwable e){
        return Default.apply(req,e);
    }
}
