package com.homo.core.root.msg;

import com.homo.core.root.Comp;
import com.homo.core.root.Configurable;
import com.homo.core.root.comp.GrpcMsgComp;
import com.homo.core.root.comp.HttpMsgComp;
import com.homo.core.root.comp.StreamMsgComp;
import com.homo.core.root.comp.WsMsgComp;
import com.homo.core.root.configurable.HttpMsgConfigurable;
import com.homo.core.root.configurable.StreamMsgConfigurable;
import com.homo.root.comp.*;
import com.homo.core.root.configurable.GrpcMsgConfigurable;
import com.homo.core.root.configurable.WsMsgConfigurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Msg implements Comp {
    Grpc(GrpcMsgComp.class,"Grpc异步通讯系统", GrpcMsgConfigurable.class),
    WebSocket(WsMsgComp.class,"WebSocket通讯系统", WsMsgConfigurable.class),
    Http(HttpMsgComp.class,"Http通讯系统", HttpMsgConfigurable.class),
    Stream(StreamMsgComp.class,"消息流通讯系统,rabbitMQ or Kafka", StreamMsgConfigurable.class),
    ;
    Class<? extends Comp> comp;
    String describe;
    Class<? extends Configurable> config;
}
