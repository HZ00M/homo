package com.homo.root.job;

import com.homo.root.ActualComp;
import com.homo.root.Comp;
import com.homo.root.Configurable;
import com.homo.root.comp.ElasticJobComp;
import com.homo.root.configurable.ElasticConfigurable;
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
    Class<? extends ActualComp> comp;
    String describe;
    Class<? extends Configurable> config;
}
