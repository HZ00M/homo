package com.homo.relational.base;

import com.homo.core.configurable.relational.RelationalProperties;
import com.homo.core.facade.relational.schema.TableSchema;
import com.homo.core.utils.module.ServiceModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
public class ScanningCoordinator implements ServiceModule {
    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private String resourcePattern = DEFAULT_RESOURCE_PATTERN;
    @Autowired
    RelationalProperties relationalProperties;
    private final List<TypeFilter> includeFilters = new LinkedList<>();
    private final List<TypeFilter> excludeFilters = new LinkedList<>();
    /**
     * MetadataReaderFactory 的作用
     * MetadataReaderFactory 是 Spring 提供的一个接口，用于创建和管理 MetadataReader 的实例。
     * MetadataReader 是 Spring 用于读取类文件（通常是 .class 文件）元数据的核心工具，通过它可以访问类的注解、方法信息和其他字节码信息。
     *
     * 主要用途：
     * 在组件扫描过程中，MetadataReaderFactory 用于高效地访问和读取类的元数据信息，而无需加载类到 JVM。
     */
    private  MetadataReaderFactory metadataReaderFactory;
    /**
     * ResourcePatternResolver 主要用于支持从类路径、文件系统等位置根据特定的资源模式（通常是路径模式）加载资源集合。
     * 例如，它可以解析 classpath: 或 file: 开头的路径，并支持通配符模式，如 classpath*:com/example/*.xml 来加载指定目录下的所有 XML 文件。
     */
    private  ResourcePatternResolver resourcePatternResolver;
    private  Environment environment;

    @Override
    public void moduleInit() {
        //CachingMetadataReaderFactory 继承自 MetadataReaderFactory，
        // 并通过缓存机制优化了类元数据的读取过程。它缓存了每个类的元数据（MetadataReader）
        // 以避免每次都重复解析类的 .class 文件。缓存的使用可以显著提升类扫描的效率，特别是在大量类被扫描时。
        metadataReaderFactory = new CachingMetadataReaderFactory();
        //ResourcePatternUtils.getResourcePatternResolver(null) 通过 null 参数传入，实际上是调用默认的
        // ResourcePatternResolver 实现，通常是 PathMatchingResourcePatternResolver。该方法会返回一个适用于当前环境的 ResourcePatternResolver 实例，通常用于资源扫描和加载。
        resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(null);
        //StandardEnvironment 是 Spring 中环境相关的一个实现类，提供了访问环境的机制，特别是在 Spring 中的
        // 应用上下文（ApplicationContext）中，它通常用于存储和访问与环境相关的配置信息，如操作系统属性、Java 系统属性、环境变量、Spring 配置文件中的属性等。
        environment = new StandardEnvironment();
    }

    public void addIncludeFilter(TypeFilter includeFilter){
        includeFilters.add(includeFilter);
    }

    public void addExcludeFilter(TypeFilter excludeFilter){
        excludeFilters.add(0, excludeFilter);
    }

    public void setResourcePattern(String resourcePattern) {
        Assert.notNull(resourcePattern, "'resourcePattern' must not be null");
        this.resourcePattern = resourcePattern;
    }

    public void resetFilters() {
        this.includeFilters.clear();
        this.excludeFilters.clear();
    }

    public Set<Class<?>> findCandidateClasses(String basePackage){
        Set<Class<?>> classes = new LinkedHashSet<>();
        try {
            String classNameToResourcePath = ClassUtils.convertClassNameToResourcePath(environment.resolveRequiredPlaceholders(basePackage));
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + classNameToResourcePath + "/" + resourcePattern;
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (isCandidateComponent(metadataReader)){
                    Class<?> clazz = Class.forName(metadataReader.getClassMetadata().getClassName());
                    classes.add(clazz);
                }
            }
        } catch (Exception e) {
            log.error("findCandidateClasses exception: ", e);
        }
        return classes;
    }

    protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException{
        for (TypeFilter excludeFilter : excludeFilters) {
            if (excludeFilter.match(metadataReader,metadataReaderFactory)){
                return false;
            }
        }
        for (TypeFilter includeFilter : includeFilters) {
            if (includeFilter.match(metadataReader,metadataReaderFactory)){
                return true;
            }
        }
        return false;
    }

}
