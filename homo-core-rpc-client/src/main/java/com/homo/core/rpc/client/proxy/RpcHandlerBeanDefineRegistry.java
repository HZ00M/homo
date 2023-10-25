package com.homo.core.rpc.client.proxy;

import com.homo.core.facade.rpc.RpcHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
public class RpcHandlerBeanDefineRegistry implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, ApplicationContextAware, EnvironmentAware {
    private ApplicationContext applicationContext;
    private ResourcePatternResolver resourcePatternResolver;
    private CachingMetadataReaderFactory cachingMetadataReaderFactory;
    private Environment environment;
    private String serviceFacadePackage;
    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    /**
     * 搜索所有被serviceExport注解，并且没有被实现的类，将其包装为rpcProxy注入到spring中去
     *
     * @param registry
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        serviceFacadePackage = environment.getProperty("homo.service.facade.package.path", "com.homo");
        Set<Class<?>> resourceClazz = scannerClassResource(serviceFacadePackage);
        registerRpcHandlerBean(registry,resourceClazz);
    }

    private void registerRpcHandlerBean(BeanDefinitionRegistry registry, Set<Class<?>> resourceClazz) {
        for (Class<?> rpcHandlerInterface : resourceClazz) {
            if (rpcHandlerInterface.isAnnotationPresent(RpcHandler.class)){
                //获取接口的所有子类
                String[] implementNames = applicationContext.getBeanNamesForType(rpcHandlerInterface, true, false);
                //如果子类数量大于0，说明是本地服务
                if (implementNames.length > 0){
                    continue;
                }
                //开始构建rpcHandler bean
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(rpcHandlerInterface);
                GenericBeanDefinition definition = (GenericBeanDefinition)builder.getRawBeanDefinition();
                /// 在这里，我们可以给该对象的属性注入对应的实例。
                /// 比如mybatis，就在这里注入了dataSource和sqlSessionFactory，
                /// 注意，如果采用definition.getPropertyValues()方式的话，
                /// 类似 definition.getPropertyValues().add("interfaceType", beanClazz);
                /// 则要求在FactoryBean（本应用中即RpcHandlerFactoryBean）提供setter方法，否则会注入失败
                /// 如果采用definition.getConstructorArgumentValues()，
                /// 则FactoryBean中需要提供包含该属性的构造方法，否则会注入失败
                definition.getConstructorArgumentValues().addGenericArgumentValue(rpcHandlerInterface);

                //这里是是生成bean实例的工厂，不是bean本身
                //FactoryBean是一种特殊的Bean,其返回对象不是指定类的一个实例
                //其返回的是该工厂Bean的getObject所返回的对象
                definition.setBeanClass(RpcHandlerFactoryBean.class);
                definition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE,rpcHandlerInterface);

                //这里采用byType方式注入，类似的还有byName等
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                registry.registerBeanDefinition(rpcHandlerInterface.getSimpleName(),definition);
            }
        }
    }

    private Set<Class<?>> scannerClassResource(String serviceFacadePackage) {
        Set<Class<?>> set = new LinkedHashSet<>();
        String basePackage = ClassUtils.convertClassNameToResourcePath(environment.resolvePlaceholders(serviceFacadePackage));
        String searchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePackage + "/" + DEFAULT_RESOURCE_PATTERN;
        try {
            Resource[] resources = this.resourcePatternResolver.getResources(searchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()){
                    //获取resource的类信息，并将其加入集合
                    MetadataReader metadataReader = this.cachingMetadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();
                    //反射（如Class.forName("")）会对类进行初始化，给静态变量赋值
                    Class<?> clazz = Class.forName(className);
                    set.add(clazz);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("scannerClassResource error ", e);
        }
        log.info("scannerClassResource searchPath {} class size {}",searchPath,set.size());
        return set;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        //获取项目所有文件资源
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.cachingMetadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
