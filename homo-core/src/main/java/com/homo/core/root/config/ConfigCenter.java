package com.homo.core.root.config;

import com.homo.core.root.Comp;
import com.homo.core.root.Configurable;
import com.homo.core.root.comp.ApolloConfigComp;
import com.homo.core.root.configurable.ApolloConfigurable;
import com.homo.core.root.configurable.HomoConfigConfigurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ConfigCenter implements Comp {
    Apollo(ApolloConfigComp.class,"Apollo配置中心，基本配置信息共享", ApolloConfigurable.class),
    HomoConfig(HomoConfigComp.class,"游戏内配置中心，游戏内配置共享", HomoConfigConfigurable.class)
    ;
    Class<? extends Comp> comp;
    String describe;
    Class<? extends Configurable> autoConfigClazz;
}
