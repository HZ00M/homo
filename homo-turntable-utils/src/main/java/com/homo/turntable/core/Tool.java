package com.homo.turntable.core;


import com.alibaba.fastjson.JSON;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class Tool {
    @Setter
   private static ToolConfig toolConfig;

    public static void deleteFile(File file){
        if (file.isFile()){
            file.delete();
        }else{//不为文件，则为文件夹
            String[] childFilePath = file.list();
            for (String path: childFilePath){
                File childFile= new File(file.getAbsoluteFile()+"/"+path);
                deleteFile(childFile);
            }
            file.delete();
        }
    }

    public static void run() throws Exception{
        // 生成 tpf_ignore_file.json
        log.info("开始处理文件 toolConfig {}", toolConfig );
        if (StringUtils.hasText(toolConfig.getIgnore()) && !toolConfig.getIgnore().equals("IGNORE")) {
            String ignoreDir = replaceParentheses(toolConfig.getJsonPath());
            createDirectory(ignoreDir.toLowerCase());
            String ignoreFile = ignoreDir + "\\homo_ignore_file.json";
            File file = new File(ignoreFile);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException("创建忽略文件失败");
                }
            }
            FileWriter fileWriter = new FileWriter(file);
            List<String> ignoreList = Arrays.asList(toolConfig.getIgnore().split(","));
            fileWriter.write(JSON.toJSONString(ignoreList));
            fileWriter.flush();
            fileWriter.close();
        }
        try {
            traverseFiles(toolConfig.getDataPath(), "", "");
        } catch (Throwable e) {
            log.error("处理异常", e);
        }
        log.info("处理完成");
    }

    /**
     * 递归遍历查找符合后缀名的文件
     * @param dataPath         如果文件类型是目录则继续递归，并传入这个目录名
     * @param subPath      基础路径后面的子路径
     * @throws Exception
     */
    public static void traverseFiles(String dataPath, String subPath, String subPackageName) throws Exception {
        String javaDir = replaceParentheses(toolConfig.getJavaPath() + subPath);
        String jsonDir = replaceParentheses(toolConfig.getJsonPath() + subPath);
        createDirectory(javaDir.toLowerCase());
        createDirectory(jsonDir.toLowerCase());
        File file = new File(dataPath);
        File[] files = file.listFiles();
        if (files == null) {
            log.info("目录为空：{}", file.getPath());
            return;
        }
        List<File> sortedFiles = Arrays.stream(files).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
        for(File f: sortedFiles){
            String fileName = f.getName();
            String logName = subPath + File.separator + fileName;
            if (logName.startsWith(File.separator)) {
                logName = logName.substring(1);
            }
            log.info("开始处理文件：{}" , logName);
            // 排除掉以 . 开头的文件
            if (fileName.startsWith(".")) {
                log.info("跳过隐藏文件：{}" , logName);
                continue;
            }
            if(f.isDirectory()){
                if(fileName.startsWith("mer_")){
                    //合并表
                    log.info("开始合并表：{}" , logName);
                    String realFileName = fileName.replace("mer_", "");
                    List<CSVRecord> csvRecords = CsvMergeUtil.mergeTable(f.getPath());
                    ToolConfig copy = toolConfig.clone();
                    copy.setJavaPath(replaceParentheses(toolConfig.getJavaPath() + subPath).toLowerCase());
                    copy.setPackageName(replaceParentheses(toolConfig.getPackageName()).toLowerCase());
                    copy.setJsonPath(jsonDir.toLowerCase() + File.separator +realFileName+".json");
                    CsvCodeGenTool csvCodeGenTool = new CsvCodeGenTool(copy, toolConfig.getFtlClassFileName(), csvRecords);
                    csvCodeGenTool.export(realFileName, toolConfig.isStopOnError());
                }else {
                    //递归
                    traverseFiles(dataPath + File.separator + f.getName(), subPath + File.separator + f.getName(), "." + f.getName());
                }
            }else{
                // 获取文件后缀名
                String[] split = fileName.split("\\.");
                if (split.length < 2) {
                    log.info("文件名不合法: {}", logName);
                    continue;
                }
                // 如果文件名中包含空格，则跳过
                if (fileName.contains(" ")) {
                    log.error("文件名中包含空格，跳过：{}", logName);
                    if (!toolConfig.isStopOnError()) {
                        continue;
                    } else {
                        throw new RuntimeException("文件名中包含空格，跳过: " + logName);
                    }
                }
                if(split[split.length - 1].equals(toolConfig.getSuffix())){
                    if(split[1].equals("csv")){
                        forCsv(split, fileName, toolConfig.getDataPath() + subPath, toolConfig.getJavaPath() + subPath, jsonDir.toLowerCase(), toolConfig.getPackageName() + subPackageName);
                    }else{
                        forExcel(split, fileName, toolConfig.getDataPath() + subPath, toolConfig.getJavaPath() + subPath, jsonDir.toLowerCase(), toolConfig.getPackageName() + subPackageName);
                    }
                    log.info("处理完成文件：{}" , logName);
                } else {
                    log.info("跳过非指定后缀的文件: {}", logName);
                }
            }
        }
    }

    public static void createDirectory(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        Files.createDirectories(path);
    }

    public static void forExcel(String[] split, String fileName, String dataPath, String javaPath, String jsonPath, String packageName) throws Exception{
        ToolConfig config = toolConfig.clone();
        config.setJavaPath(replaceParentheses(javaPath).toLowerCase());
        config.setPackageName(packageName);
        config.setJsonPath(jsonPath + "\\"+split[0]+".json");
        config.setDataPath(dataPath);
        config.setSeparator("\\|");
        if(split[0].equals("ServerEnumConfig")){
            //枚举类的生成
            String ftlFile = toolConfig.getFtlEnumFileName();
            ExclEnumGenTool tool = new ExclEnumGenTool(config, ftlFile);
            tool.export(fileName, toolConfig.isStopOnError());
        }else {
            String ftlFile = toolConfig.getFtlClassFileName();
            ExclCodeGenTool tool = new ExclCodeGenTool(config, ftlFile);
            tool.export(fileName, toolConfig.isStopOnError());
        }
    }

    public static void forCsv(String[] split, String fileName, String dataPath, String javaPath, String jsonPath, String packageName) {
        ToolConfig config = toolConfig.clone();
        config.setJavaPath(replaceParentheses(javaPath).toLowerCase());
        config.setPackageName(replaceParentheses(packageName).toLowerCase());
        config.setJsonPath(jsonPath + "\\"+split[0]+".json");
        config.setDataPath(dataPath);
        if(split[0].equals("ServerEnumConfig")){
            //枚举类的生成
            String ftlFile = toolConfig.getFtlEnumFileName();
            CsvEnumGenTool tool = new CsvEnumGenTool(config, ftlFile);
            tool.export(fileName, toolConfig.isStopOnError());
        }else {
            String ftlFile = toolConfig.getFtlClassFileName();
            CsvCodeGenTool csvCodeGenTool = new CsvCodeGenTool(config, ftlFile);
            csvCodeGenTool.export(fileName, toolConfig.isStopOnError());
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

    /**
     * 从配置文件中读取配置
     * @param args 入口参ovt
     */
    public static void loadConfig(String[] args) {
        // 读取 properties 配置文件
        log.info("读取配置文件");
        try {
            toolConfig = new ToolConfig(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
