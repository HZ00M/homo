package com.homo.turntable.core;

import freemarker.cache.TemplateLoader;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

@Slf4j
@Data
@ToString
public class ToolConfig implements Cloneable {
    private String ftlClassFileName = "exclRead.ftl";
    private String ftlEnumFileName = "exclEnumRead.ftl";
    private String ftlPath = "FTL_PATH";
    private String javaPath = "CODE_PATH";
    private String jsonPath = "JSON_PATH";
    private String packageName = "PACKAGE_NAME";
    private String dataPath = "EXCEL_PATH";
    private String cellType = "SUFFUX_NAME";
    private String filter = "FLAG";
    private String suffix = "suffix";
    private String separator = "SEPARATOR";

    private String ignore = "IGNORE";

    private boolean isPackageType = true;

    private boolean stopOnError = false;

    private boolean notExportWhenOnlyKeys = true;

    private TemplateLoader templateLoader;

    public ToolConfig() {

    }

    public ToolConfig(String ftlPath,String ftlClassFileName,String ftlEnumFileName,
                      String javaPath, String jsonPath, String packageName,
                      String dataPath, String cellType, String filter, String suffix, String separator,
                      String ignore, boolean isPackageType, boolean stopOnError, boolean notExportWhenOnlyKeys,
                      TemplateLoader templateLoader) {
        this.ftlPath = ftlPath;
        this.javaPath = javaPath;
        this.jsonPath = jsonPath;
        this.packageName = packageName;
        this.dataPath = dataPath;
        this.cellType = cellType;
        this.filter = filter;
        this.suffix = suffix;
        this.separator = separator;
        this.ignore = ignore;
        this.isPackageType = isPackageType;
        this.stopOnError = stopOnError;
        this.notExportWhenOnlyKeys = notExportWhenOnlyKeys;
        this.templateLoader = templateLoader;
    }

    public ToolConfig(String[] args) {
        if (args.length == 1) {
            String configFile = args[0];
            loadConfig(configFile);
        } else {
            ftlPath = args[0];
            javaPath = args[1];
            packageName = args[2];
            dataPath = args[3];
            cellType = args[4];
            filter = args[5];
            jsonPath = args[6];
            separator = args[7];
            suffix = args[8];
            if (args.length > 9) {
                ignore = args[9];
            }
            if (args.length > 10) {
                isPackageType = Boolean.parseBoolean(args[10]);
            }
            if (args.length > 11) {
                stopOnError = Boolean.parseBoolean(args[11]);
            }
            if (args.length > 12) {
                notExportWhenOnlyKeys = Boolean.parseBoolean(args[12]);
            }
        }
        log.info("文件后缀名：{}", suffix);
        log.info("分割符：{}", separator);
        log.info("模板文件路径：{}", ftlPath);
        log.info("输出代码路径：{}", javaPath);
        log.info("生成代码的包名：{}", packageName);
        log.info("Excel路径：{}", dataPath);
        log.info("生成文件后缀名：{}", cellType);
        log.info("需要的字段：{}", filter);
        log.info("json目录：{}", jsonPath);
        log.info("忽略文件：{}", ignore);
        log.info("是否使用包装类型：{}", isPackageType);
        log.info("出错时停止：{}", stopOnError);
    }

    public void loadConfig(String configFile) {
        // 读取 properties 配置文件
        log.info("读取配置文件：{}", configFile);
        try {
            Properties properties = new Properties();
            File configFileObj = new File(configFile);
            if (!configFileObj.exists()) {
                throw new RuntimeException("配置文件不存在：" + configFile);
            }
            properties.load(Files.newInputStream(configFileObj.toPath()));
            // 读取配置
            ftlPath = properties.getProperty(ftlPath);
            javaPath = properties.getProperty(javaPath);
            jsonPath = properties.getProperty(jsonPath);
            packageName = properties.getProperty(packageName);
            dataPath = properties.getProperty(dataPath);
            cellType = properties.getProperty(cellType);
            filter = properties.getProperty(filter);
            separator = properties.getProperty(separator);
            suffix = properties.getProperty(suffix);
            isPackageType = Boolean.parseBoolean(properties.getProperty("IS_PACKAGE_TYPE", "true"));
            ignore = properties.getProperty(ignore, "IGNORE");
            stopOnError = Boolean.parseBoolean(properties.getProperty("STOP_ON_ERROR", "false"));
            notExportWhenOnlyKeys = Boolean.parseBoolean(properties.getProperty("NOT_EXPORT_WHEN_ONLY_KEYS", "true"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ToolConfig clone() {
        try {
            ToolConfig clone = (ToolConfig) super.clone();
            clone.ftlPath = this.ftlPath;
            clone.templateLoader = this.templateLoader;
            clone.javaPath = this.javaPath;
            clone.jsonPath = this.jsonPath;
            clone.packageName = this.packageName;
            clone.dataPath = this.dataPath;
            clone.cellType = this.cellType;
            clone.filter = this.filter;
            clone.separator = this.separator;
            clone.suffix = this.suffix;
            clone.ignore = this.ignore;
            clone.isPackageType = this.isPackageType;
            clone.stopOnError = this.stopOnError;
            clone.notExportWhenOnlyKeys = this.notExportWhenOnlyKeys;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
