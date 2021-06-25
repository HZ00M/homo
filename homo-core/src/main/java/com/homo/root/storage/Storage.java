package com.homo.root.storage;

import com.homo.root.ActualComp;
import com.homo.root.Comp;
import com.homo.root.Configurable;
import com.homo.root.comp.MongoStorageComp;
import com.homo.root.configurable.MongoConfigurable;
import com.homo.root.configurable.MysqlConfigurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Storage implements Comp {
    Mysql(MongoStorageComp.class,"Mysql存储系统", MysqlConfigurable.class),
    Mongo(MongoStorageComp.class,"Mongo存储系统", MongoConfigurable.class);
    Class<? extends ActualComp> comp;
    String describe;
    Class<? extends Configurable> config;
}
