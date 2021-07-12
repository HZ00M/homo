package com.homo.core.root.job;

import com.homo.core.root.Comp;
import com.homo.core.root.Configurable;
import com.homo.core.root.configurable.ElasticConfigurable;
import com.homo.core.root.comp.ElasticJobComp;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Job implements Comp {
    ElasticJob(ElasticJobComp.class,"分布式调度", ElasticConfigurable.class),
    ;
    Class<? extends Comp> comp;
    String describe;
    Class<? extends Configurable> config;
}
