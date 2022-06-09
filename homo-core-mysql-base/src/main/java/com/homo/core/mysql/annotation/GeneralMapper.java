package com.homo.core.mysql.annotation;


import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 通用mapper
 *
 * @param <T>
 */
public interface GeneralMapper<T> {
    @SelectProvider(type = SQLGen.class, method = "create")
    void create(T t, String tableName);

    @SelectProvider(type = SQLGen.class, method = "drop")
    void drop(T t, String tableName);

    @InsertProvider(type = SQLGen.class, method = "insert")
    int add(T t, String tableName);

    @DeleteProvider(type = SQLGen.class, method = "delete")
    int del(T t, String tableName);

    @UpdateProvider(type = SQLGen.class, method = "update")
    int update(T t, String tableName);

    @SelectProvider(type = SQLGen.class, method = "select")
    List<T> select(T t, String tableName);
}
