package com.homo.turntable.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置表的前四行表头配置
 * 第一行：字段名称
 * 第二行：字段类型
 * 第三行：字段备注
 * 第四行：字段标记 K,A,S,C
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableMetaInfo {
    private Map<Integer, String> fieldDefine = new HashMap<>();
    private Map<Integer, String> flagDefine = new HashMap<>();
    private Map<Integer, String> typeDefine = new HashMap<>();
    private Map<Integer, String> commonDefine = new HashMap<>();

    private boolean newVersion = false;
}
