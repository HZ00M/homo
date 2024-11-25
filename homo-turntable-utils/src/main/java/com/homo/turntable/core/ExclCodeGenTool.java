package com.homo.turntable.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

@Slf4j
public class ExclCodeGenTool extends AbsCodeGenTool {

    String sheetName = null;

    public static FormulaEvaluator formulaEvaluator;

    public static String META_INFO = "__META_INFO__";
    public static String ROW_INDEX = "__ROW_INDEX__";

    /**
     * 表头信息, key: sheetIndex, value: TableMetaInfo
     */
    Map<Integer, TableMetaInfo> tableMetaInfos = new HashMap<>();

    private Workbook workbook;

    public static final int MAX_ROW = 100000;

    @Getter
    private boolean singleTableMode;

    public ExclCodeGenTool(ToolConfig toolConfig, String ftlFileName) {
        super(toolConfig, ftlFileName);
    }

    @Override
    public String getExtrName(String fileName) {
        String[] split = fileName.split("\\.", 2);
        return "." + split[1];
    }

    @Override
    protected void extrParam(Map root) {
        if (sheetName != null) {
            root.put("sheetName", sheetName);
        } else {
            root.put("sheetName", "null");
        }
    }

    private boolean isType(String type) {
        // 基本类型
        // int long float double boolean
        // 包装类型
        // Integer Long Float Double Boolean
        // String
        // BigDecimal
        // List，Map，Set，JSONObject，JSONArray
        return type.equals("Integer") || type.equals("Long") || type.equals("Float") || type.equals("Double") || type.equals("Boolean") || type.equals("String") || type.equals("BigDecimal") || type.startsWith("List") || type.startsWith("Map") || type.startsWith("Set") || type.equals("JSONObject") || type.equals("JSONArray") || type.equals("int") || type.equals("long") || type.equals("float") || type.equals("double") || type.equals("boolean") || type.equals("byte") || type.equals("short") || type.equals("bool") || type.equals("string") || type.startsWith("e") || type.equalsIgnoreCase("json");
    }

    public TableMetaInfo readSheetHeader(Sheet sheet) {
        // 读取表头
        TableMetaInfo tableMetaInfo = new TableMetaInfo();
        Map<Integer, String> fieldDefine = tableMetaInfo.getFieldDefine();
        Map<Integer, String> flagDefine = tableMetaInfo.getFlagDefine();
        Map<Integer, String> typeDefine = tableMetaInfo.getTypeDefine();
        Map<Integer, String> commonDefine = tableMetaInfo.getCommonDefine();
        tableMetaInfo.setNewVersion(isNewVersion(sheet));
        if (tableMetaInfo.isNewVersion()) {
            Row filedRecord = sheet.getRow(0);
            Row typeRecord = sheet.getRow(1);
            Row commonRecord = sheet.getRow(2);
            Row flagRecord = sheet.getRow(3);
            readFieldDefine(filedRecord, 1, fieldDefine);
            readTypeDefine(typeRecord, fieldDefine.size(), 1, typeDefine);
            readFlagDefine(flagRecord, fieldDefine.size(), flagDefine);
            readCommonDefine(commonRecord, fieldDefine.size(), commonDefine);
        } else {
            Row filedRecord = sheet.getRow(0);
            Row typeRecord = sheet.getRow(1);
            readFieldDefine(filedRecord, 0, fieldDefine);
            readTypeDefine(typeRecord, fieldDefine.size() - 1, 0, typeDefine);
            flagDefine.put(0, "K");
            Row commonRecord = sheet.getRow(2);
            readCommonDefine(commonRecord, fieldDefine.size(), commonDefine);
        }

        return tableMetaInfo;
    }

    private boolean isSheetShouldExport(TableMetaInfo header) {
        // fieldDefine 不为空， 且flagDefine中有K
        Map<Integer, String> fieldDefine = header.getFieldDefine();
        Map<Integer, String> flagDefine = header.getFlagDefine();
        Map<Integer, String> typeDefine = header.getTypeDefine();
        if (fieldDefine.isEmpty()) {
            return false;
        }
        if (typeDefine.isEmpty()) {
            return false;
        }

        for (Map.Entry<Integer, String> entry : flagDefine.entrySet()) {
            if (entry.getValue().contains("K")) {
                return true;
            }
        }
        return false;
    }



    private boolean isDefineSame(int i, Map<Integer, String> source, Map<Integer, String> other) {
        if (!source.equals(other)) {
            // 输出不同的字段
            boolean equals = true;
            for (Map.Entry<Integer, String> entry : source.entrySet()) {
                if (!other.containsKey(entry.getKey())) {
//                    log.warn("sheet {} 中没有列: {}, value: {}", i, indexToName(entry.getKey()), entry.getValue());
                    equals = false;
                    continue;
                }
                if (!other.get(entry.getKey()).equals(entry.getValue())) {
                    equals = false;
//                    log.warn("sheet {} 中列：{} value: {} 不等于 {}" , i, indexToName(entry.getKey()), other.get(entry.getKey()), entry.getValue());
                }
            }
            return equals;
//            throw new RuntimeException("需要导出的sheet中的表头不相同");
        }
        return true;
    }

    public boolean isMetaInfoSame(int sheetIndex, TableMetaInfo source, TableMetaInfo other) {
        if (!isDefineSame(sheetIndex, source.getFieldDefine(), other.getFieldDefine())) {
            return false;
        }
        if (!isDefineSame(sheetIndex, source.getFlagDefine(), other.getFlagDefine())) {
            return false;
        }
        return isDefineSame(sheetIndex, source.getTypeDefine(), other.getTypeDefine());
    }

    private void logDifferentDefine(int sheetIndex, Map<Integer, String> source, Map<Integer, String> other) {
        for (Map.Entry<Integer, String> entry : source.entrySet()) {
            if (!other.containsKey(entry.getKey())) {
                log.warn("sheet {} 中没有列: {}, value: {}", sheetIndex, indexToName(entry.getKey()), entry.getValue());
                continue;
            }
            if (!other.get(entry.getKey()).equals(entry.getValue())) {
                log.warn("sheet {} 中列：{} value: {} 不等于 {}" , sheetIndex, indexToName(entry.getKey()), other.get(entry.getKey()), entry.getValue());
            }
        }
    }

    public void determineTableMode() {
        // 如果只有一个sheet，那么就是单表模式
        if (tableMetaInfos.size() == 1) {
            singleTableMode = true;
            return;
        }
        // 先假设是单表模式
        singleTableMode = true;
        // 如果每个sheet的表结构都相同，那么也是单表模式
        List<Integer> keys = new ArrayList<>(tableMetaInfos.keySet());
        TableMetaInfo first = tableMetaInfos.get(keys.get(0));
        for (int i = 1; i < keys.size(); i++) {
            TableMetaInfo other = tableMetaInfos.get(keys.get(i));
            if (!isMetaInfoSame(keys.get(i), first, other)) {
                singleTableMode = false;
            }
        }

        if (singleTableMode) {
            return;
        }
        // 如果有多个sheet，且每个sheet的表结构不同，那么就是多表模式
        // 但是如果有一个sheet的表结构和其他sheet相同，那么就是混合模式，混和模式是不支持的
        for (int i = 0; i < keys.size(); i++) {
            TableMetaInfo metaInfo = tableMetaInfos.get(keys.get(i));
            for (int j = i + 1; j < keys.size(); j++) {
                TableMetaInfo other = tableMetaInfos.get(keys.get(j));
                if (isMetaInfoSame(keys.get(j), metaInfo, other)) {
                    logDifferentDefine(keys.get(i), metaInfo.getFieldDefine(), other.getFieldDefine());
                    logDifferentDefine(keys.get(i), metaInfo.getFlagDefine(), other.getFlagDefine());
                    logDifferentDefine(keys.get(i), metaInfo.getTypeDefine(), other.getTypeDefine());
                    throw new RuntimeException("混合模式不支持");
                }
            }
        }
        singleTableMode = false;
    }


    @Override
    public void initFromStream(String fileName, InputStream in) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook(in);
        initWorkbook(fileName, workbook);
    }

    public void initWorkbook(String fileName, Workbook workbook) throws Exception {
        this.fileName = fileName;
        if (workbook instanceof XSSFWorkbook) {
            formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        }
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int sheetIndex = 0; sheetIndex < numberOfSheets; sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            TableMetaInfo header = readSheetHeader(sheet);
            if (!isSheetShouldExport(header)) {
                continue;
            }
            // 读取表头
            tableMetaInfos.put(sheetIndex, header);
        }
        this.workbook = workbook;
        determineTableMode();
    }

    public ExportClassInfo getExportClassInfo(int sheetIndex, TableMetaInfo tableMetaInfo) throws Exception {
        //用来判断这个表是不是一个空表，只有K的字段没有其他的字段
        ExportClassInfo exportClassInfo = new ExportClassInfo();

        List<Map<String, String>> fieldInfoList = new LinkedList<>();
        for (Map.Entry<Integer, String> field : tableMetaInfo.getFieldDefine().entrySet()) {
            Map<String, String> fieldInfo = new HashMap<>();
            String name = field.getValue();
            String type = tableMetaInfo.getTypeDefine().get(field.getKey());
            String common = tableMetaInfo.getCommonDefine().getOrDefault(field.getKey(), "");
            String flag = tableMetaInfo.getFlagDefine().getOrDefault(field.getKey(), "A");
            if ("K".equals(flag) && (type.startsWith("List") || type.startsWith("Map") || type.startsWith("Set") || type.startsWith("Array"))) {
                throw new RuntimeException("K类型的字段不支持集合类型");
            }
            if("K".equals(flag)){
                Map<String, String> keyTypeMap = new HashMap<>();
                keyTypeMap.put("keyName", name);
                keyTypeMap.put("keyType", type);
                exportClassInfo.getKeyTypeList().add(keyTypeMap);
                exportClassInfo.setKeyName(name);
                exportClassInfo.setKeyType(type);
            }
            if("".equals(flag) || null == flag || "K".equals(flag) || "A".equals(flag) || flag.contains(toolConfig.getFilter())) {
                fieldInfo.put("name", name);
                fieldInfo.put("type", type);
                fieldInfo.put("common", common);
                fieldInfo.put("flag", flag);
                fieldInfoList.add(fieldInfo);
            }
        }
        if (singleTableMode) {
            exportClassInfo.setClassName(getClassName(fileName));
            exportClassInfo.setFileName(fileName);
        } else {
            String className = getClassName(workbook.getSheetName(sheetIndex));
            exportClassInfo.setClassName(className);
            exportClassInfo.setFileName(className + getExtrName(fileName));
        }
        exportClassInfo.setFieldInfoList(fieldInfoList);
        return exportClassInfo;
    }

    @Override
    public void exportClasses() throws Exception {
        for (Map.Entry<Integer, TableMetaInfo> tableHeaderEntry : tableMetaInfos.entrySet()) {
            int sheetIndex = tableHeaderEntry.getKey();
            TableMetaInfo tableMetaInfo = tableHeaderEntry.getValue();
            ExportClassInfo classInfo = getExportClassInfo(sheetIndex, tableMetaInfo);
            export(classInfo);
            if (singleTableMode) {
                // 单表模式下只导出一个表结构对应的class就行了
                break;
            }
        }
    }

    /**
     * key 表名
     * value 表数据
     * @return
     */
    public List<ExportDataInfo> buildJson() throws Exception {
        List<ExportDataInfo> exportDataInfos = new ArrayList<>();
        if (singleTableMode) {
            JSONObject jsonResult = new JSONObject(new LinkedHashMap<>());
            List<Integer> sheetIndexes = new LinkedList<>();
            for (Map.Entry<Integer, TableMetaInfo> tableHeaderEntry : tableMetaInfos.entrySet()) {
                int sheetIndex = tableHeaderEntry.getKey();
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                log.info("开始导出sheet:{}", sheet.getSheetName());
                buildJson(sheetIndex, sheet, jsonResult);
                sheetIndexes.add(sheetIndex);
                log.info("sheet:{}导出完成", sheet.getSheetName());
            }
            ExportDataInfo exportDataInfo = new ExportDataInfo();
            exportDataInfo.setTableName(getClassName(fileName));
            exportDataInfo.setSheetIndexes(sheetIndexes);
            exportDataInfo.setData(jsonResult);
            exportDataInfos.add(exportDataInfo);
        } else {
            for (Map.Entry<Integer, TableMetaInfo> tableHeaderEntry : tableMetaInfos.entrySet()) {
                int sheetIndex = tableHeaderEntry.getKey();
                JSONObject jsonResult = new JSONObject(new LinkedHashMap<>());
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                log.info("开始导出sheet:{}", sheet.getSheetName());
                buildJson(0, sheet, jsonResult);
                String tableName = getClassName(workbook.getSheetName(sheetIndex));
                ExportDataInfo exportDataInfo = new ExportDataInfo();
                exportDataInfo.setTableName(tableName);
                exportDataInfo.setSheetIndexes(Collections.singletonList(sheetIndex));
                exportDataInfo.setData(jsonResult);
                exportDataInfos.add(exportDataInfo);
            }
        }
        return exportDataInfos;
    }

    @Override
    public void exportJson() throws Exception {
        List<ExportDataInfo> tableDatas = buildJson();
        for (ExportDataInfo tableData : tableDatas) {
            String tableName = tableData.getTableName();
            JSONObject jsonResult = tableData.getData();
            if (toolConfig.getFilter().equalsIgnoreCase("C")) {
                // 去除 meta 信息
                // 去除 行 信息
                jsonResult.remove(ExclCodeGenTool.META_INFO);
                jsonResult.forEach((key, value) -> {
                    JSONObject jsonObject = (JSONObject) value;
                    jsonObject.remove(ExclCodeGenTool.ROW_INDEX);
                });
            }
            String path = replaceParentheses(toolConfig.getJsonPath());
            path = path.substring(0, path.lastIndexOf(File.separator));
            String fileName = path + File.separator + tableName + ".json";
            JsonUtil.write(jsonResult, fileName);
        }
    }

    boolean isNewVersionRow(Row record) {
        if (isRowEmpty(record)) {
            return false;
        }
        for (int c = record.getFirstCellNum(); c < record.getLastCellNum(); c++) {
            Cell cell = record.getCell(c);
            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                continue;
            }
            if (cell.getCellType() != CELL_TYPE_STRING) {
                return false;
            }
            String cellValue = cell.getStringCellValue();
            String flags = "KANSC";
            if (!flags.contains(cellValue)) {
                return false;
            }
        }
        return true;
    }

    boolean isNewVersionColumn(Sheet sheet) {
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 0; i <= lastRowNum; ++i) {
            if (isRowEmpty(sheet.getRow(i))) {
                continue;
            }
            Cell cell = sheet.getRow(i).getCell(0);
            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                continue;
            }
            if (cell.getCellType() != CELL_TYPE_STRING) {
                return false;
            }
            String cellValue = cell.getStringCellValue();
            String flags = "N";
            if (!flags.contains(cellValue)) {
                return false;
            }
        }
        return true;
    }

    public boolean isNewVersion(Sheet sheet) {
        boolean isRowNew = isNewVersionRow(sheet.getRow(3));
        boolean isColumnNew = isNewVersionColumn(sheet);

        return isRowNew && isColumnNew;
    }

    void readFieldDefine(Row fieldRow,int index, Map<Integer, String> fields) {
        if (isRowEmpty(fieldRow)) {
            return;
        }
        Cell filedCell = fieldRow.getCell(index);
        while (!isCellEmpty(filedCell)){
            filedCell.setCellType(Cell.CELL_TYPE_STRING);
            String filedValue = filedCell.getStringCellValue().trim();
            fields.put(index, filedValue);
            index++;
            filedCell = fieldRow.getCell(index);
        }
    }

     void readTypeDefine(Row typeRow, int fieldCount, int index, Map<Integer, String> fields) {
        if (isRowEmpty(typeRow)) {
            return;
        }
        for (int i = index; i <= fieldCount; ++i) {
            Cell flagCell = typeRow.getCell(i);
            if (isCellEmpty(flagCell)) {
                continue;
            }
            flagCell.setCellType(Cell.CELL_TYPE_STRING);
            String typeValue = flagCell.getStringCellValue().trim();
            if (isType(typeValue)) {
                fields.put(i, typeValue);
            } else {
                log.warn("type:{} is not support", typeValue);
            }
        }
    }

     void readFlagDefine(Row flagRow, int fieldCount, Map<Integer, String> fields) {
        if (isRowEmpty(flagRow)) {
            return;
        }
        for (int i = 1; i <= fieldCount; ++i) {
            Cell flagCell = flagRow.getCell(i);
            if  (isCellEmpty(flagCell)) {
                continue;
            }
            flagCell.setCellType(Cell.CELL_TYPE_STRING);
            String flag = flagCell.getStringCellValue();
            fields.put(i, flag);
        }
    }
    void readCommonDefine(Row flagRow, int fieldCount, Map<Integer, String> fields) {
        if (isRowEmpty(flagRow)) {
            return;
        }
        for (int i = 0; i <= fieldCount; ++i) {
            Cell flagCell = flagRow.getCell(i);
            if (isCellEmpty(flagCell)) {
                continue;
            }
            flagCell.setCellType(Cell.CELL_TYPE_STRING);
            String flag = flagCell.getStringCellValue().trim();
            fields.put(i, flag);
        }
    }

    // 将 Map<Integer, String> 转换为 Map<String, String>
    private JSONObject convertMap(boolean isNewVersion, Map<Integer, String> map) {
        JSONObject fieldInfo = new JSONObject(new LinkedHashMap<>());
        map.forEach((index, field) -> {
            if (isNewVersion) {
                fieldInfo.put(String.valueOf(index), field);
            } else {
                fieldInfo.put(String.valueOf(index + 1), field);
            }
        });
        return fieldInfo;
    }
    private JSONArray buildMetaInfo(TableMetaInfo tableMetaInfo) {
        boolean isNewVersion = tableMetaInfo.isNewVersion();
        Map<Integer, String> fieldDefine = tableMetaInfo.getFieldDefine();
        Map<Integer, String> typeDefine = tableMetaInfo.getTypeDefine();
        Map<Integer, String> commonDefine = tableMetaInfo.getCommonDefine();
        Map<Integer, String> flagDefine = tableMetaInfo.getFlagDefine();
        JSONArray meta = new JSONArray();
        JSONObject fieldInfo = convertMap(isNewVersion, fieldDefine);
        meta.add(fieldInfo);
        JSONObject typeInfo = convertMap(isNewVersion, typeDefine);
        meta.add(typeInfo);
        JSONObject commonInfo = convertMap(isNewVersion, commonDefine);
        meta.add(commonInfo);
        JSONObject flagInfo = convertMap(isNewVersion, flagDefine);
        meta.add(flagInfo);
        return meta;
    }

    /**
     * 读取数据转化成json
     *
     * @param sheet
     */
    public void buildJson(int sheetIndex, Sheet sheet, JSONObject result) throws Exception {
        int lastRowNum = sheet.getLastRowNum();
        TableMetaInfo tableMetaInfo = tableMetaInfos.get(workbook.getSheetIndex(sheet));
        result.put(META_INFO, buildMetaInfo(tableMetaInfo));
        int startRow = 3;
        int startIndex = 0;
        if (tableMetaInfo.isNewVersion()) {
            startRow = 4;
            startIndex = 1;
        }
        for(int i = startRow; i <= lastRowNum; i++){
            if (i % 1000 == 0) {
                log.info("当前进度》》》》" + i + "行");
            }
            Row record = sheet.getRow(i);
            if(isRowEmpty(record)){
               continue;
            }
            if (tableMetaInfo.isNewVersion()) {
                Cell cell1 = record.getCell(0);
                if(cell1 != null && cell1.getCellType() != CELL_TYPE_BLANK){
                    if ("N".equals(cell1.getStringCellValue())) {
                        // 不需要的数据
                        continue;
                    }
                }
            }
            buildOneRow(sheetIndex, i, record, startIndex, tableMetaInfo.getFieldDefine(), tableMetaInfo.getTypeDefine(), tableMetaInfo.getFlagDefine(), result);
        }
    }

    public static int getSheetIndexFromRowIndex(int rowIndex) {
        return rowIndex / MAX_ROW;
    }

    public static int getSheetRowIndex(int rowIndex) {
        return rowIndex % MAX_ROW;
    }

    public static int getRowIndex(int sheetIndex, int rowIndex) {
        return rowIndex + sheetIndex * MAX_ROW;
    }

    /**
     * 读取每一行数据转化成json
     * @param rowIndex
     * @param record
     * @param filedRecord
     * @param typeRecord
     * @param flagRecord
     * @param result
     */
    private void buildOneRow(int currentSheetIndex, int rowIndex,Row record, int columnIndex, Map<Integer, String> filedRecord, Map<Integer, String> typeRecord, Map<Integer, String> flagRecord, JSONObject result) throws Exception {
        StringBuilder keyBuilder = new StringBuilder();
        JSONObject data = new JSONObject(new LinkedHashMap<>());
        data.put(ROW_INDEX, getRowIndex(currentSheetIndex, rowIndex));
        int index = columnIndex;
        try {
            for (index = columnIndex; index <= filedRecord.size(); ++index) {
                Cell cell = record.getCell(index);
                String flag = flagRecord.get(index);
                String filedValue = filedRecord.get(index);
                if (filedValue == null) {
                    continue;
                }
                String typeValue = typeRecord.get(index);
                if("K".equals(flag)){
                    //如果主键为空不读取改行
                    if(cell == null){
                        return;
                    }
                    cell.setCellType(CELL_TYPE_STRING);
                    String value = cell.getStringCellValue();
                    keyBuilder.append("_").append(value);
                }
                if("".equals(flag) || "K".equals(flag) || "A".equals(flag) || flag == null || flag.contains(toolConfig.getFilter())){
                    //判断数据结构类型
                    if (isCellEmpty(cell)) {
                        data.put(filedValue, formatToType( typeValue, null));
                        continue;
                    }
                    String cellValue = getCellValue(cell);
                    data.put(filedValue, formatToType(typeValue, cellValue));
                }

            }

        }
        catch (Exception e){
            log.error("第{}行 {}列数据解析异常, key: {}", rowIndex + 1, indexToName(index), keyBuilder, e);
            throw new Exception("第" + (rowIndex + 1) + "行第" + indexToName(index) + "列数据解析异常", e);
        }
        String key = keyBuilder.substring(1);
        if(!StringUtils.hasText(key)){
            return;
        }
        if(result.containsKey(key)){
            log.error("重复的key：{}", key);
            throw new Exception("重复的key：" + key);
        }
        result.put(key, data);
    }

    private String getCellValue(Cell cell) {
        String cellValue = "";
        // 判断单元格数据的类型
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC: // 数字
                DataFormatter formatter = new DataFormatter();
                cellValue = formatter.formatCellValue(cell);
                break;
            case Cell.CELL_TYPE_STRING: // 字符串
                cellValue = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_BOOLEAN: // Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA: // 公式
                // 计算公式
//                try {
//                    cellValue = evalCell(cell);
//                }catch (Exception e) {
                    if (cell instanceof XSSFCell) {
                        XSSFCell xssfCell = (XSSFCell) cell;
                        cellValue = xssfCell.getRawValue();
                    } else {
                        log.error("ERROR on formula evaluation, column: {}, row: {}", cell.getColumnIndex(), cell.getRowIndex());
                        throw new RuntimeException("ERROR on formula evaluation");
                    }
//                }
                break;
            case Cell.CELL_TYPE_BLANK: // 空值
                cellValue = "";
                break;
            case Cell.CELL_TYPE_ERROR: // 故障
                // 抛出运行时异常
                log.error("ERROR on formula evaluation, column: {}, row: {}", cell.getColumnIndex(), cell.getRowIndex());
                throw new RuntimeException("ERROR on formula evaluation");
            default:
                // 抛出运行时异常
                log.error("Unknown cell type, column: {}, row: {}", cell.getColumnIndex(), cell.getRowIndex());
                throw new RuntimeException("Unknown cell type");
        }
        if (StringUtils.hasText(cellValue)) {
            cellValue = cellValue.replaceAll("\\\\n", "\n");
            cellValue = cellValue.replaceAll("\\\\t", "\t");
            // /r/n 替换成/n
            cellValue = cellValue.replaceAll("\r\n", "\n");
            cellValue = cellValue.replaceAll("\\\\r\\\\n", "\n");
        }
        return cellValue;
    }


    /**
     * 判断一行是否为空
     * @param row
     * @return
     */
    private  boolean isRowEmpty(Row row){
        if(row == null){
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断一个格子是不是为空
     * @param cell
     * @return
     */
    public boolean isCellEmpty(final Cell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return true;
        }

        if (cell.getCellType() == CELL_TYPE_STRING && cell.getStringCellValue().isEmpty()) {
            return true;
        }

        return false;
    }
}
