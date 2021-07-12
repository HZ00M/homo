package com.homo.core.root.cache;

import com.homo.core.root.Comp;
import com.homo.core.root.Configurable;
import com.homo.core.root.comp.LocalCacheComp;
import com.homo.core.root.comp.RedisCacheComp;
import com.homo.core.root.configurable.RedisCacheConfigurable;
import com.homo.core.root.configurable.LocalCacheConfigurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Cache implements Comp {
    Local(LocalCacheComp.class,"本地缓存系统", LocalCacheConfigurable.class),
    Redis(RedisCacheComp.class,"redis缓存系统", RedisCacheConfigurable.class),
    ;
    Class<? extends Comp> comp;
    String describe;
    Class<? extends Configurable> config;
}
