package com.homo.core.root.discover;

import com.homo.core.root.Configurable;
import com.homo.core.root.Comp;
import com.homo.core.root.comp.DiscoverComp;
import com.homo.core.root.configurable.DiscoverConfigurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Discover implements Comp{
    ServerExport(DiscoverComp.class,"服务注册并提供门面使用", DiscoverConfigurable.class)
    ;
    Class<? extends Comp> comp;
    String describe;
    Class<? extends Configurable> config;
}
