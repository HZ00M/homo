package com.homo.turntable.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Slf4j
public class CsvEnumGenTool extends AbsCodeGenTool {


    String sheetName = null;

    InputStream inputStream;

    public CsvEnumGenTool(ToolConfig toolConfig, String ftlFileName) {
        super(toolConfig, ftlFileName);
    }

    public void makeTableClass(String fileName, String sheetName) throws Exception {
        this.sheetName = sheetName;
        export(fileName, false);
    }

    @Override
    public String getExtrName(String fileName) {
        return ".csv";
    }

    @Override
    protected void extrParam(Map root){
        if (sheetName != null) {
            root.put("sheetName", sheetName);
        } else {
            root.put("sheetName", "null");
        }

    }

    @Override
    public void initFromStream(String fileName, InputStream in) throws Exception {
        this.fileName = fileName;
        this.inputStream = in;
    }

    @Override
    public void exportClasses() throws Exception {
        export(getExportClassInfo(inputStream));
    }

    @Override
    public void exportJson() throws Exception {

    }


    public ExportClassInfo getExportClassInfo(InputStream in) throws Exception {
        ExportClassInfo exportClassInfo = new ExportClassInfo();
        exportClassInfo.setFileName(fileName);
        exportClassInfo.setClassName(getClassName(fileName));
        List<Map<String, String>> fieldInfoList = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(in, "GBK" ))) {
            CSVParser parser = CSVParser.parse(reader, CSVFormat.EXCEL);
            List<CSVRecord> records = parser.getRecords();
            //去重
            Set<String> set = new HashSet<>();
            for(int rowIndex = 3; rowIndex < records.size(); rowIndex++){
                Map<String, String> fieldInfo = new HashMap<>();
                String stringCellValue = records.get(rowIndex).get(headOff);
                if(!set.contains(stringCellValue)){
                    fieldInfo.put("TypeName", stringCellValue);
                    set.add(stringCellValue);
                }
                fieldInfo.put("Name", records.get(rowIndex).get(headOff + 1));
                fieldInfo.put("Value", records.get(rowIndex).get(headOff + 2));
                fieldInfo.put("Annotation", records.get(rowIndex).get(headOff + 3));
                fieldInfoList.add(fieldInfo);
            }
        }catch (Exception throwable){
            log.error("获取字段信息失败", throwable);
            throw throwable;
        }
        exportClassInfo.setFieldInfoList(fieldInfoList);
        return exportClassInfo;
    }
}
