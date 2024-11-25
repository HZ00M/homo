package com.homo.turntable.core;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.*;

public class ExclEnumGenTool extends AbsCodeGenTool {


    String sheetName = null;

    XSSFWorkbook workbook = null;

    public ExclEnumGenTool(ToolConfig toolConfig, String ftlFileName) {
        super(toolConfig, ftlFileName);
    }

    public void makeTableClass(String fileName, String sheetName) throws Exception {
        this.sheetName = sheetName;
        export(fileName, false);
    }

    @Override
    public String getExtrName(String fileName) {
        return ".xlsx";
    }

    @Override
    protected void extrParam(Map root){
        if (sheetName != null)
            root.put("sheetName", sheetName);
        else
            root.put("sheetName", "null");

    }

    @Override
    public void initFromStream(String fileName, InputStream in) throws Exception {
        this.fileName = fileName;
        workbook = new XSSFWorkbook(in);
    }

    @Override
    public void exportClasses() throws Exception {
        export(getExportClassInfo());
    }

    @Override
    public void exportJson() throws Exception {

    }


    public ExportClassInfo getExportClassInfo() throws Exception {
        ExportClassInfo exportClassInfo = new ExportClassInfo();
        exportClassInfo.setFileName(fileName);
        XSSFSheet sheet;
        if (sheetName != null)
            sheet = workbook.getSheet(sheetName);
        else
            sheet = workbook.getSheetAt(0);
        XSSFRow head = sheet.getRow(headOff);
        int lastRowIndex = sheet.getLastRowNum();
//        System.out.println("lastRowIndex:" + lastRowIndex);
        List<Map<String, String>> fieldInfoList = new ArrayList<>();
        //去重
        Set<String> set = new HashSet<>();
        for(int rowIndex = 3; rowIndex <= lastRowIndex; rowIndex++){
            Map<String, String> fieldInfo = new HashMap<>();
            String stringCellValue = sheet.getRow(rowIndex).getCell(headOff).getStringCellValue();
            if(!set.contains(stringCellValue)){
                fieldInfo.put("TypeName", sheet.getRow(rowIndex).getCell(headOff).toString());
                set.add(stringCellValue);
            }
            fieldInfo.put("Name", sheet.getRow(rowIndex).getCell(headOff + 1).toString());
            fieldInfo.put("Value", sheet.getRow(rowIndex).getCell(headOff + 2).toString());
            XSSFCell cell = sheet.getRow(rowIndex).getCell(headOff + 3);
            if (cell == null){
                fieldInfo.put("Annotation", sheet.getRow(rowIndex).getCell(headOff + 1).toString());
            }else {
                fieldInfo.put("Annotation", sheet.getRow(rowIndex).getCell(headOff + 3).toString());
            }
            fieldInfoList.add(fieldInfo);
        }
        exportClassInfo.setFieldInfoList(fieldInfoList);
        exportClassInfo.setClassName(getClassName(fileName));
        return exportClassInfo;
    }
}
