package com.homo.root.cache;

import com.homo.root.ActualComp;
import com.homo.root.Comp;
import com.homo.root.Configurable;
import com.homo.root.comp.LocalCacheComp;
import com.homo.root.comp.RedisCacheComp;
import com.homo.root.configurable.LocalCacheConfigurable;
import com.homo.root.configurable.RedisCacheConfigurable;
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
    Class<? extends ActualComp> comp;
    String describe;
    Class<? extends Configurable> config;
}
