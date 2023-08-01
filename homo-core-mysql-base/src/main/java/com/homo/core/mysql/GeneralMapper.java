package com.homo.core.mysql;


import com.homo.core.mysql.annotation.SQLGen;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 通用mapper
 *
 * @param <T>
 */
public interface GeneralMapper<T> {

    @SelectProvider(type = SQLGen.class, method = "create")
    void create(Class<T> t, String tableName);

    @SelectProvider(type = SQLGen.class, method = "drop")
    void drop(T t, String tableName);

    @InsertProvider(type = SQLGen.class, method = "insert")
    int add(@Param("model") T t, @Param("tableName") String tableName);

    @DeleteProvider(type = SQLGen.class, method = "delete")
    int del(@Param("model") T t, @Param("tableName") String tableName);

    @UpdateProvider(type = SQLGen.class, method = "update")
    int update(@Param("model") T t, @Param("tableName") String tableName);

    @SelectProvider(type = SQLGen.class, method = "select")
    List<T> select(T t, String tableName);
}
