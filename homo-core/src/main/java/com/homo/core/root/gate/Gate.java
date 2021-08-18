package com.homo.core.root.gate;

import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateServer;
import com.homo.core.root.Comp;
import com.homo.core.root.Configurable;
import com.homo.core.root.comp.DispatcherGateComp;
import com.homo.core.root.comp.ProxyGateComp;
import com.homo.core.root.configurable.DispatcherConfigurable;
import com.homo.core.root.configurable.ProxyGateConfigurable;
import com.homo.core.root.ActualComp;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Gate implements Comp {
    Client(GateClient.class,"保持与客户端连接，处理客户端消息，给客户端发送消息", ProxyGateConfigurable.class),
    Server(GateServer.class,"作为内部通信的服务器，负责处理客户端连接，内部Rpc消息转发", DispatcherConfigurable.class),
    ;
    Class<?> comp;
    String describe;
    Class<? extends Configurable> config;
}
