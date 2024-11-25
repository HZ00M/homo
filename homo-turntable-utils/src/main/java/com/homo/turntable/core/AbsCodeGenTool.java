package com.homo.turntable.core;

import com.alibaba.fastjson.JSONObject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbsCodeGenTool {

    final int headOff = 0;
    Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    Template template;

    ToolConfig toolConfig;

    String fileName;


    public AbsCodeGenTool(ToolConfig toolConfig, String ftlFileName) {
        // 把freemarker的jar包添加到工程中
        this.toolConfig = toolConfig;
        try {
            if (toolConfig.getTemplateLoader() != null) {
                configuration.setTemplateLoader(toolConfig.getTemplateLoader());
                configuration.setDefaultEncoding("utf-8");
//                ftlFileName = File.separator + path + File.separator + ftlFileName;
                template = configuration.getTemplate(ftlFileName, "utf-8");
            } else {
                configuration.setDirectoryForTemplateLoading(new File(toolConfig.getFtlPath()));
                configuration.setDefaultEncoding("utf-8");
                template = configuration.getTemplate(ftlFileName, "utf-8");
            }
            log.info("加载模板文件成功");
        } catch (Exception e) {
            if (!toolConfig.getFtlPath().equals("none")) {
                log.error("找不到模板文件", e);
            }
        }
    }

    protected InputStream read(String path) {
        try {
            File file = new File(toolConfig.getDataPath() + "/" + path);
            if (file.isDirectory()){
                return null;
            }
            return new FileInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract String getExtrName(String fileName);

    protected void extrParam(Map root) {
    }

    String getClassName(String fileName){
        String shortName;
        if(fileName.contains(".")){
            shortName = fileName.substring(0, fileName.length() - getExtrName(fileName).length());
        }else{
            shortName = fileName;
        }
        return replaceParentheses(shortName);
    }

    void export(ExportClassInfo classInfo) throws Exception {
        if (classInfo == null){
            return;
        }

        String fileName = replaceParentheses(classInfo.getFileName());
        String shortName = replaceParentheses(classInfo.getClassName());
        String filePath = replaceParentheses(toolConfig.getJsonPath());
        if (StringUtils.hasText(filePath)){
            filePath = filePath.substring(0, filePath.lastIndexOf(File.separator)) + File.separator + shortName + ".json";
        }

        Map<String, Object> root = new HashMap<>();
        root.put("dataPath", toolConfig.getDataPath());
        root.put("filename", fileName.split("\\.")[0]);
        root.put("className", shortName);
        root.put("fields", classInfo.getFieldInfoList());
        root.put("packagename", toolConfig.getPackageName());
        root.put("keyname", classInfo.getKeyName());
        root.put("keys", classInfo.getKeyTypeList());
        root.put("jsonPath", filePath);
        root.put("isPackageType", toolConfig.isPackageType());
        root.put("keyType", classInfo.getKeyType());
        extrParam(root);
        // 创建一个Writer对象，指定生成的文件保存的路径及文件名。
        String path = toolConfig.getJavaPath() + "/Table_" + fileName.replace(getExtrName(fileName), "") + toolConfig.getCellType();
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
        // 调用模板对象的process方法生成静态文件。需要两个参数数据集和writer对象。

        template.process(root, out);
        // 关闭writer对象。
        out.flush();
        out.close();
    }

    //public abstract List getFieldInfoList(InputStream in) throws Exception;

    public abstract void initFromStream(String fileName, InputStream in) throws Exception;

    public abstract void exportClasses() throws Exception;

    public abstract void exportJson() throws Exception;

    public void export(String fileName, boolean stopOnError) {
        try {
            InputStream inputStream = read(fileName);
            initFromStream(fileName, inputStream);
            exportClasses();
            exportJson();
//            List<?> fieldInfoList = getFieldInfoList(inputStream);
//            makeTableClass(fieldInfoList, fileName);
        }catch (Exception e){
            log.error("makeTableClass error, fileName:{}", fileName, e);
            if (stopOnError){
                throw new RuntimeException(e);
            }
        }
    }

//    /**
//     * 设置默认数据
//     * @param filedValue
//     * @param typeValue
//     * @param data
//     */
//    public void BuildDefaultData(String filedValue, String typeValue, JSONObject data) throws Exception {
//        if(typeValue.contains("List"))
//        {
//            List list = new ArrayList<>();
//            data.put(filedValue, list);
//        }else if(typeValue.contains("Map")){
//            Map map = new HashMap();
//            data.put(filedValue, map);
//        }else{
//            data.put(filedValue, buildTypeDefaultValue("", typeValue));
//        }
//    }

    public Object buildTypeDefaultValue(String str, String type) throws Exception{
        if ("int".equals(type)){
            return 0;
        }else if("string".equals(type)){
            return "";
        }else if("bool".equals(type)){
            return false;
        }else if("float".equals(type)){
            if (toolConfig.getFilter().equalsIgnoreCase("c")) {
                return 0f;
            }
           return "0";
        } else if ("double".equals(type)) {
            if (toolConfig.getFilter().equalsIgnoreCase("c")) {
                return 0d;
            }
            return "0";
        } else if ("date".equals(type)) {
            return new Date();
        } else if ("datetime".equals(type)) {
            return new Date();
        } else if ("time".equals(type)) {
            return new Date();
        } else if ("timestamp".equals(type)) {
            return new Date();
        } else if ("decimal".equals(type)) {
            return new BigDecimal("0.00");
        } else if("long".equals(type)){
            if (!StringUtils.hasText(str)){
                str = "0";
            }
            return Long.parseLong(str);
        }else if(type.startsWith("e")){
            return 0;
        } else if (type.startsWith("List")) {
            return new ArrayList<>();
        } else if (type.startsWith("Map")) {
            return new HashMap<>();
        } else if (type.startsWith("json")) {
            return new JSONObject();
        } else{
            log.error("没有这个类型:{}", type);
            throw new Exception("没有这个类型");
        }
    }

    Object formatToType(String type, String cellValue) throws Exception {
        if (cellValue == null || cellValue.isEmpty()) {
            return buildTypeDefaultValue(null, type);
        }
        if (type.startsWith("int")) {
            return Integer.parseInt(cellValue);
        } else if (type.startsWith("long")) {
            return Long.parseLong(cellValue);
        } else if (type.startsWith("float")) {
            if (cellValue.endsWith("%")) {
                float v = Float.parseFloat(cellValue.substring(0, cellValue.length() - 1)) / 100;
                if (toolConfig.getFilter().equalsIgnoreCase("c")) {
                    return v;
                }
                return String.valueOf(v);
            }
            float v = Float.parseFloat(cellValue);
            if (toolConfig.getFilter().equalsIgnoreCase("c")) {
                return v;
            }
            return cellValue;
        } else if (type.startsWith("double")) {
            if (cellValue.endsWith("%")) {
                double v =  Double.parseDouble(cellValue.substring(0, cellValue.length() - 1)) / 100;
                if (toolConfig.getFilter().equalsIgnoreCase("c")) {
                    return v;
                }
                return String.valueOf(v);
            }
            double v = Double.parseDouble(cellValue);
            if (toolConfig.getFilter().equalsIgnoreCase("c")) {
                return v;
            }
            return  cellValue;
        } else if (type.startsWith("bool")) {
            switch (cellValue.toLowerCase()) {
                case "0":
                case "false":
                case "f":
                    return false;
                case "1":
                case "true":
                case "t":
                    return true;
            }
            throw new RuntimeException("bool类型配置错误，只能是0或1或true或false或TRUE或FALSE，当前值为：" + cellValue);
        } else if (type.startsWith("string")) {
            return cellValue;
        } else if (type.startsWith("json")) {
            return JSONObject.parseObject(cellValue);
        } else if (type.startsWith("List")) {
            // 利用正则获取泛型类型
            String regex = "<(.*)>";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(type);
            if (matcher.find()) {
                String generic = matcher.group(1);
                String[] split = cellValue.split(toolConfig.getSeparator());
                List<Object> list = new ArrayList<>();
                for (String s : split) {
                    list.add(formatToType(generic, s)); // 递归
                }
                return list;
            }
            return Arrays.asList(cellValue.split(toolConfig.getSeparator()));
        } else if (type.startsWith("Map")) {
            // 利用正则获取泛型类型
            String regex = "<(.*)>";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(type);
            if (matcher.find()) {
                String generic = matcher.group(1);
                // 分别获取key和value的类型
                String keyType = generic.substring(0, generic.indexOf(","));
                String valueType = generic.substring(generic.indexOf(",") + 1);
                String[] split = cellValue.split("#");
                Map<Object, Object> map = new HashMap<>();
                for (String s : split) {
                    String[] kv = s.split("\\|");
                    map.put(String.valueOf(formatToType(keyType, kv[0])), formatToType(valueType, kv[1])); // 递归
                }
                return map;
            }
            // 没有泛型类型，直接返回
            return JSONObject.parseObject(cellValue);
        } else if (type.startsWith("e")) {
            // 检查cellValue是否是数字
            if (cellValue.matches("\\d+")) {
                return Integer.parseInt(cellValue);
            }
            return cellValue;
        } else {
            return cellValue;
        }
    }

    /**
     * 去除字符串里面的括号内容
     * @param s
     * @return
     */
    public static String replaceParentheses(String s) {
        if(!StringUtils.isEmpty(s)){
            s = s.replaceAll("\\(.*?\\)|\\{.*?}|\\[.*?]|（.*?）", "");
            return s;
        }
        return s;
    }

    public String indexToName(int index) {
        // 0 -> A
        // 26 -> AA
        if (index < 26) {
            return String.valueOf((char) (index + 65));
        } else {
            return String.valueOf((char) (index / 26 + 64)) + String.valueOf((char) (index % 26 + 65));
        }
    }
}
