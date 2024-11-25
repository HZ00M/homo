package com.homo.turntable.core;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class ExportDataInfo {
    private String tableName;
    private List<Integer> sheetIndexes;
    private JSONObject data;
}
