package com.homo.core.root.storage;

import com.homo.core.root.Comp;
import com.homo.core.root.Configurable;
import com.homo.core.root.comp.MongoStorageComp;
import com.homo.core.root.comp.MysqlStorageComp;
import com.homo.core.root.configurable.MongoConfigurable;
import com.homo.core.root.configurable.MysqlConfigurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Storage implements Comp {
    Mongo(MongoStorageComp.class,"Mongo存储系统", MongoConfigurable.class),
    Mysql(MysqlStorageComp.class,"Mysql存储系统", MysqlConfigurable.class);
    Class<? extends Comp> comp;
    String describe;
    Class<? extends Configurable> config;
}
