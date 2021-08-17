package com.homo.core.root;

import com.homo.core.facade.Driver;
import com.homo.core.facade.cache.CacheDriver;
import com.homo.core.root.cache.Cache;
import com.homo.core.root.config.ConfigCenter;
import com.homo.core.root.configurable.*;
import com.homo.core.root.gate.Gate;
import com.homo.core.root.job.Job;
import com.homo.core.root.msg.Msg;
import com.homo.core.root.discover.Discover;
import com.homo.core.root.storage.Storage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Sys {
    Discover_Sys(Discover.class, "服务发现", AnnotateConfigurable.class,Driver.class),
    Storage_Sys(Storage.class,"存储系统", StorageConfigurable.class,Driver.class),
    Cache_Sys(Cache.class,"缓存系统", CacheConfigurable.class,CacheDriver.class),
    Msg_Sys(Msg.class,"消息系统", MsgConfigurable.class,Driver.class),
    Config_Sys(ConfigCenter.class,"配置中心", ConfigCenterConfigurable.class,Driver.class),
    Job_Sys(Job.class,"调度中心", JobConfigurable.class,Driver.class),
    Gate_Sys(Gate.class,"网关服务", GateConfigurable.class,Driver.class)
    ;
    Class<? extends Comp> comp;
    String describe;
    Class<? extends Configurable> config;
    Class<? extends Driver> driver;
}
