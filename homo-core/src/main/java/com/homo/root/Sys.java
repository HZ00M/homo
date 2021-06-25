package com.homo.root;

import com.homo.root.cache.Cache;
import com.homo.root.config.ConfigCenter;
import com.homo.root.configurable.*;
import com.homo.root.gate.Gate;
import com.homo.root.job.Job;
import com.homo.root.msg.Msg;
import com.homo.root.storage.Storage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Sys {
    Storage_Sys(Storage.class,"存储系统", StorageConfigurable.class),
    Cache_Sys(Cache.class,"缓存系统", CacheConfigurable.class),
    Msg_Sys(Msg.class,"消息系统", MsgConfigurable.class),
    Config_Sys(ConfigCenter.class,"配置中心", ConfigCenterConfigurable.class),
    Job_Sys(Job.class,"调度中心", JobConfigurable.class),
    Gate_Sys(Gate.class,"网关服务",GateConfigurable.class)
    ;
    Class<? extends Comp> comp;
    String describe;
    Class<? extends Configurable> config;
}
