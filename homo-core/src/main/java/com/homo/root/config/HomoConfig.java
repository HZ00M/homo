package com.homo.root.config;

import com.homo.root.Comp;
import com.homo.root.comp.HomoConfigClientComp;
import com.homo.root.comp.HomoConfigServerComp;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum HomoConfig implements Comp {
    Config_Client(HomoConfigClientComp.class,"配置中心客户端"),
    Config_Server(HomoConfigServerComp.class,"配置中心服务端")
    ;
    Class<? extends Comp> comp;
    String describe;
}
