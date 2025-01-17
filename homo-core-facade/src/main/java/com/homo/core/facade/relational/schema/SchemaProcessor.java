package com.homo.core.facade.relational.schema;

import com.homo.core.utils.rector.Homo;

import java.util.Set;

public interface SchemaProcessor {
    /**
     * 加载已经存在的表
     */
    void loadExistingTables();

    /**
     * 判断一个表是否存在
     */
    boolean tableExists(TableSchema table);

    /**
     * 创建一个表
     * @param table 表结构
     */
    void createTable(TableSchema table);


    /**
     * 创建一个表
     * @param table 表结构
     */
    Homo<Boolean> createTablePromise(TableSchema table);

    /**
     * 获取所有的表名
     * @return 表名列表
     */
    Set<String> getTableNameList();
}
