package com.homo.core.maven.mojo;

import com.homo.core.maven.BuildConfiguration;
import com.homo.core.maven.ConfigKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.factory.annotation.Value;

/**
 * defaultValue = "${project}"：设置了默认值为${project}，表示该参数默认值为当前项目的MavenProject对象。
 * required = true：指定该参数为必需参数，在执行Mojo时如果未提供该参数则会报错。
 * 通过将MavenProject对象作为Mojo的参数，在插件执行过程中可以获取和操作当前项目的相关信息。例如，可以使用project.getBasedir()获取项目根目录路径等。
 * <p>
 * 同时，通过Lombok生成的getter方法可以方便地访问该属性。例如，在其他方法中可以通过调用getProject()来获取当前项目对象。
 */
@Slf4j
public abstract class AbsHomoMojo<T extends AbsHomoMojo> extends AbstractMojo {


    /**
     * Maven Mojo（Maven Plain Old Java Object）是单例的。在 Maven 构建过程中，
     * 每个 Mojo 类都是作为插件目标来执行的。Mojo 是由 Maven 在运行时实例化，并且在整个构建过程中只会有一个实例存在。
     * 由于Mojo是独立的  所以可以这么做
     * 将该插件设置到静态字段上，方便其他对象对插件信息的访问
     */
    public static AbsHomoMojo tpfMojo;

    /**
     * 自动注入工程信息
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Getter
    @Parameter(defaultValue = "${project}",required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "false",property = "homo-build-skip")
    protected boolean skip;

    public AbsHomoMojo(){
        tpfMojo = this;
    }
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip){
            log.info("skip homo build");
            return;
        }
        System.setProperty(ConfigKey.PROJECT_BASE_DIR,project.getBasedir().getAbsolutePath());
        doExecute();
    }

    abstract protected void doExecute() throws MojoFailureException;

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(){
        return (T) tpfMojo;
    }

    /**
     * 检查模块名与工程文件名是否相等
     * @return
     */
    public boolean artEqualBaseDirName(){
        String artifactId = project.getArtifact().getArtifactId();
        String projectDirName = project.getBasedir().getName();
        return artifactId.equals(projectDirName);
    }

    public  String getProjectName(){
        return project.getName();
    }

    public void checkDirName() throws MojoFailureException {
        if (!tpfMojo.artEqualBaseDirName()) {
            String errorMsg = String.format("art name %s not equal base dir name %s", tpfMojo.getProject().getArtifactId(), tpfMojo.getProject().getBasedir().getName());
            throw new MojoFailureException(errorMsg);
        }
    }

}
