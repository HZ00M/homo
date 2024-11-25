package com.homo.core.maven.mojo;

import com.homo.turntable.core.Tool;
import com.homo.turntable.core.ToolConfig;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;

@Mojo(name = "homoTurnTable",
        defaultPhase = LifecyclePhase.NONE,
        requiresDependencyCollection = ResolutionScope.COMPILE)
@Slf4j
public class TurnTableMojo extends AbsHomoMojo<TurnTableMojo> {
    //    String fltPath = "D:\\code\\tpf-game-tool\\tpf-turntable-tool\\src\\test\\resources\\ftl";
    //    String javaPath = "D:\\code\\tpf-game-tool\\tpf-turntable-tool\\src\\test\\java\\test";
    //    String packageName = "test";
    //    String dataPath = "D:\\code\\tpf-game-tool\\tpf-turntable-tool\\src\\test\\resources\\excel";
    //
    //    String jsonPath = "D:\\code\\tpf-game-tool\\tpf-turntable-tool\\src\\test\\resources\\json";
    //    String suffixName = ".java";
    //    String filter = "S";
    //
    //    String suffix = "xlsx";
    //    String separator = "|";
//    @Parameter(property = "batFile",defaultValue = "src/main/resources/homo_turn_table.bat")
//    String batFile;
    @Parameter(property = "fltPath")
    String fltPath;
    @Parameter(property = "ftlClassFileName",defaultValue = "exclRead.ftl")
    String ftlClassFileName;
    @Parameter(property = "ftlEnumFileName",defaultValue = "exclEnumRead.ftl")
    String ftlEnumFileName;
    @Parameter(property = "javaPath", required = true)
    String javaPath;
    @Parameter(property = "packageName", required = true)
    String packageName;
    @Parameter(property = "dataPath", required = true)
    String dataPath;
    @Parameter(property = "jsonPath", required = true)
    String jsonPath;
    @Parameter(property = "codeType", defaultValue = ".java")
    String codeType;
    @Parameter(property = "filter", defaultValue = "A")
    String filter;
    @Parameter(property = "suffix", defaultValue = "xlsx")
    String suffix;
    @Parameter(property = "separator", defaultValue = "|")
    String separator;
    @Parameter(property = "ignore",defaultValue = "IGNORE")
    String ignore;
    @Parameter(property = "isPackageType", defaultValue = "true")
    boolean isPackageType ;
    @Parameter(property = "stopOnError", defaultValue = "false")
    boolean stopOnError ;
    @Parameter(property = "notExportWhenOnlyKeys", defaultValue = "true")
    boolean notExportWhenOnlyKeys ;
    
    @Override
    protected void doExecute() throws MojoFailureException {
        try {
            ToolConfig toolConfig ;
            TemplateLoader templateLoader;
            if (StringUtils.isEmpty(fltPath)){
                templateLoader = new ClassTemplateLoader(getClass(), "/ftl");
            }else {
                templateLoader = new FileTemplateLoader(new File(fltPath));
            }
            toolConfig = new ToolConfig(
                    fltPath,
                    ftlClassFileName,
                    ftlEnumFileName,
                    javaPath,
                    jsonPath,
                    packageName,
                    dataPath,
                    codeType,
                    filter,
                    suffix,
                    separator,
                    ignore,
                    isPackageType,
                    stopOnError,
                    notExportWhenOnlyKeys,
                    templateLoader);
            Tool.setToolConfig(toolConfig);
            Tool.run();
        }catch (Exception e){
            throw new MojoFailureException(e.getMessage());
        }
    }
}
