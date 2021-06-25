package com.homo.root.config;

import com.homo.root.Comp;
import com.homo.root.Configurable;
import com.homo.root.comp.ApolloConfigComp;
import com.homo.root.configurable.ApolloConfigurable;
import com.homo.root.configurable.HomoConfigConfigurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ConfigCenter implements Comp {
    Apollo(ApolloConfigComp.class,"Apollo配置中心，基本配置信息共享", ApolloConfigurable.class),
    HomoConfig(HomoConfig.class,"游戏内配置中心，游戏内配置共享", HomoConfigConfigurable.class)
    ;
    Class<? extends Comp> comp;
    String describe;
    Class<? extends Configurable> config;
}
