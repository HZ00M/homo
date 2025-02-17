package com.homo.core.facade.relational.operation;

import com.homo.core.utils.rector.Homo;

import java.util.List;
import java.util.Map;

/**
 * 执行sql语句接口
 */
public interface ExecuteOperation {
    ExecuteSpec execute(String sql);

    interface ExecuteSpec{
        Homo<Map<String,Object>> one();

        Homo<Map<String,Object>> first();

        Homo<Integer> rowsUpdated();

        Homo<List<Map<String,Object>>> all();
    }
}
