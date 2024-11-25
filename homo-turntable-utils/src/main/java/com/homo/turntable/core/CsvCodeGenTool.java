package com.homo.turntable.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.homo.turntable.core.error.FormatException;
import com.homo.turntable.core.error.KeyRepeatedException;
import com.homo.turntable.core.error.NoKeyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class CsvCodeGenTool extends AbsCodeGenTool {

    List<CSVRecord> records;

//    String separator;

    List<CSVRecord> data;

    public CsvCodeGenTool(ToolConfig toolConfig, String ftlFileName) {

        super(toolConfig, ftlFileName);
//        this.separator = separator;
    }

    public CsvCodeGenTool(ToolConfig toolConfig, String ftlFileName, List<CSVRecord> data) {
        super(toolConfig, ftlFileName);
//        this.separator = separator;
        this.data = data;
    }

    @Override
    public String getExtrName(String fileName) {
        return ".csv";
    }

    @Override
    public void initFromStream(String fileName, InputStream in) throws Exception {
        this.fileName = fileName;
        if(data == null){
            Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8 ));
            CSVParser parser = CSVParser.parse(reader, CSVFormat.EXCEL);
            records = parser.getRecords();
        }else{
            records = data;
        }
    }

    @Override
    public void exportClasses() throws Exception {
        ExportClassInfo exportClassInfo = getExportClassInfo();
        if (exportClassInfo != null) {
            export(exportClassInfo);
        } else {
            log.info("no export class info: {}", fileName);
        }
    }

    @Override
    public void exportJson() throws Exception {
        ExportClassInfo exportClassInfo = getExportClassInfo();
        if (exportClassInfo != null) {
            JSONObject jsonResult = buildJson(records);
            if (toolConfig.getFilter().equalsIgnoreCase("C")) {
                // 去除 meta 信息
                // 去除 行 信息
                jsonResult.remove(ExclCodeGenTool.META_INFO);
                jsonResult.forEach((key, value) -> {
                    JSONObject jsonObject = (JSONObject) value;
                    jsonObject.remove(ExclCodeGenTool.ROW_INDEX);
                });
            }
            JsonUtil.write(jsonResult, replaceParentheses(toolConfig.getJsonPath()));
        } else {
            log.info("there is no need to export json, since no class is export: {}", fileName);
        }
    }

    public ExportClassInfo getExportClassInfo() throws Exception {
        try {
            ExportClassInfo exportClassInfo = new ExportClassInfo();
            exportClassInfo.setFileName(fileName);
            exportClassInfo.setClassName(getClassName(fileName));
            List<Map<String, String>> fieldInfoList = new ArrayList<>();
            //用来判断这个表是不是一个空表，只有K的字段没有其他的字段
            boolean emptyFieldInfoList = true;

            int fieldCount = records.get(headOff).size();
            // 从config对象中获得模板对象。需要制定一个模板文件的名字。
            for (int i = 1; i < fieldCount; i++) {
                Map<String, String> fieldInfo = new HashMap<>();
                String flag = records.get(headOff + 3).get(i);
                //K=主键 A=全都有 I自增主键 filter=bat处理文件定义所需要的
                String name = records.get(headOff).get(i);
                String type = records.get(headOff + 1).get(i);
                if("K".equals(flag) || "I".equals(flag)){
                    Map<String, String> keyTypeMap = new HashMap<>();
                    keyTypeMap.put("keyName", name);
                    keyTypeMap.put("keyType", type);
                    exportClassInfo.getKeyTypeList().add(keyTypeMap);
                    exportClassInfo.setKeyName(name);
                    exportClassInfo.setKeyType(type);
                }
                if (null == flag || "".equals(flag) || "K".equals(flag) || "A".equals(flag) || "I".equals(flag) || flag.contains(toolConfig.getFilter())) {
                    if("A".equals(flag) || Objects.requireNonNull(flag).contains(toolConfig.getFilter()) || "".equals(flag)){
                        emptyFieldInfoList = false;
                    }
                    fieldInfo.put("name", name);
                    fieldInfo.put("type", type);
                    fieldInfo.put("common", records.get(headOff + 2).get(i));
                    fieldInfo.put("flag", flag);
                    fieldInfoList.add(fieldInfo);
                }
            }
            if(emptyFieldInfoList && toolConfig.isNotExportWhenOnlyKeys()){
                //如果是空表就没有必要转出来了
                return null;
            }

            exportClassInfo.setFieldInfoList(fieldInfoList);
            return exportClassInfo;
        }catch (Throwable throwable){
            throw throwable;
        }
    }

    Map<Integer, String> readDefine(CSVRecord row) {
        Map<Integer, String> defines = new HashMap<>();
       int columnIndex = 1;
       while (columnIndex < row.size()) {
           String cell = row.get(columnIndex);
           defines.put(columnIndex, cell);
           columnIndex++;
       }
       return defines;
    }

    public JSONObject buildJson(List<CSVRecord> records) throws Exception {
        try {
            CSVRecord filedRecord = records.get(0);
            CSVRecord typeRecord = records.get(1);
            CSVRecord commonRecord = records.get(2);
            CSVRecord flagRecord = records.get(3);
            int lastRowNum = records.size();
            JSONObject result = new JSONObject(new LinkedHashMap<>());
            Map<Integer, String> fieldDefine = readDefine(filedRecord);
            Map<Integer, String> flagDefine = readDefine(flagRecord);
            Map<Integer, String> typeDefine = readDefine(typeRecord);
            Map<Integer, String> commonDefine = readDefine(commonRecord);
            JSONArray meta = new JSONArray();
            JSONObject fieldInfo = new JSONObject();
            fieldDefine.forEach((index, field) -> {
                fieldInfo.put(String.valueOf(index), field);
            });
            meta.add(fieldInfo);
            JSONObject typeInfo = new JSONObject();
            typeDefine.forEach((index, field) -> {
                typeInfo.put(String.valueOf(index), field);
            });
            meta.add(typeInfo);
            JSONObject commonInfo = new JSONObject();
            commonDefine.forEach((index, common) -> {
                commonInfo.put(String.valueOf(index), common);
            });
            meta.add(commonInfo);
            JSONObject flagInfo = new JSONObject();
            flagDefine.forEach((index, flag) -> {
                flagInfo.put(String.valueOf(index), flag);
            });
            meta.add(flagInfo);
            result.put(ExclCodeGenTool.META_INFO, meta);
            Map<String, Integer> keyLineMap = new HashMap<>();
            for(int i = 4; i < lastRowNum; i++){
                CSVRecord record = records.get(i);
                if (record == null){
                    continue;
                }
                buildOneRow(i, record, filedRecord, typeRecord, flagRecord, result, keyLineMap);
            }
            return result;
        }catch (Exception e){
            log.error("build csv format error: ", e);
            throw e;
        }
    }

    private void buildOneRow(int rowIndex, CSVRecord record, CSVRecord filedRecord, CSVRecord typeRecord, CSVRecord flagRecord, JSONObject result, Map<String, Integer> keyLineMap) throws Exception {
        StringBuilder keyBuilder = new StringBuilder();
        JSONObject data = new JSONObject(new LinkedHashMap<>());

        int firstColumn = 0;
        int columnIndex = 1;
        try {
            if(isRowEmpty(record)){
                return;
            }
            String firstColumnValue = record.get(firstColumn);
            if("N".equals(firstColumnValue)){
                //一整行数据都不要
                return;
            }
            while (columnIndex < filedRecord.size()){
                String cell = record.get(columnIndex);
                String flag = flagRecord.get(columnIndex);
                String filedValue = filedRecord.get(columnIndex);
                String typeValue = typeRecord.get(columnIndex);
                if("K".equals(flag)){
                    //如果主键为空不读取改行
                    if(StringUtils.isEmpty(cell)){
                        return;
                    }
                    keyBuilder.append("_").append(cell);
                }
                if("I".equals(flag)){
                    //I代表程序控制自增id
                    keyBuilder.append("_").append(rowIndex - 4);
                }
                if("".equals(flag) || "K".equals(flag) || "A".equals(flag) || flag.contains(toolConfig.getFilter())){
                    //判断数据结构类型
                    if (!StringUtils.hasText(cell)) {
                        data.put(filedValue, formatToType(typeValue, null));
                        columnIndex++;
                        continue;
                    }
                    data.put(filedValue, formatToType(typeValue, cell));
                }
                columnIndex++;
            }
            if (keyBuilder.length() == 0) {
                throw new NoKeyException(rowIndex + "行没有找到主键");
            }
            String key = keyBuilder.toString().substring(1);
            if(result.containsKey(key)){
                String message = String.format("重复的key: %s, 所在行：%s, %s", key, rowIndex + 1, keyLineMap.get(key));
                throw new KeyRepeatedException(message);
            }
            keyLineMap.put(key, rowIndex + 1);
            result.put(key, data);
        }
        catch (Exception e){
            if (e instanceof KeyRepeatedException || e instanceof NoKeyException) {
                log.error(e.getMessage(), e);
                throw e;
            }
            String cellValue = "";
            if (record.isSet(columnIndex)) {
                cellValue = record.get(columnIndex);
            }
            String message = String.format("第%s行 %s列数据解析异常, key: %s, 列要求类型：%s, 列所填的值：%s", rowIndex + 1, indexToName(columnIndex), keyBuilder, typeRecord.get(columnIndex), cellValue);
            log.error("第{}行 {}列数据解析异常, key: {}, 列要求类型：{}, 列所填的值：{}", rowIndex + 1, indexToName(columnIndex), keyBuilder, typeRecord.get(columnIndex), cellValue,  e);
            throw new FormatException(message, e);
        }
    }

    private boolean isRowEmpty(CSVRecord row){
        if(row == null){
            return true;
        }
        for (int c = 0; c < row.size(); c++) {
            String value = row.get(c);
            if(!StringUtils.isEmpty(value)){
                return false;
            }
        }
        return true;
    }
}