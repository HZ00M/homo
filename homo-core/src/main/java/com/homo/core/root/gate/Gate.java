package com.homo.core.root.gate;

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
    Dispatcher(DispatcherGateComp.class,"内部寻址转发", DispatcherConfigurable.class),
    Proxy(ProxyGateComp.class,"外部消息代理", ProxyGateConfigurable.class),
    ;
    Class<? extends ActualComp> comp;
    String describe;
    Class<? extends Configurable> config;
}
