package com.homo.root.gate;

import com.homo.root.ActualComp;
import com.homo.root.Comp;
import com.homo.root.Configurable;
import com.homo.root.comp.DispatcherGateComp;
import com.homo.root.comp.ProxyGateComp;
import com.homo.root.configurable.DispatcherConfigurable;
import com.homo.root.configurable.ProxyGateConfigurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Gate implements Comp {
    Dispatcher(DispatcherGateComp.class,"内部寻址转发", DispatcherConfigurable.class),
    Proxy(ProxyGateComp.class,"外部消息代理", ProxyGateConfigurable.class),
    ;
    Class<? extends ActualComp> comp;
    String describe;
    Class<? extends Configurable> config;
}
