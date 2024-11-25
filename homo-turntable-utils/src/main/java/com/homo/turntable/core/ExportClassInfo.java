package com.homo.turntable.core;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class ExportClassInfo {
    private String keyName;
    private String className;
    private String fileName;
    private String keyType;
    private List<Map<String, String>> keyTypeList = new ArrayList<>();
    private List<Map<String, String>> fieldInfoList = new LinkedList<>();
}
