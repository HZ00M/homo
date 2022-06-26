package com.homo.service.dirty.config;

import com.homo.service.dirty.anotation.DirtyLandingServer;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

public class DirtySelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages("com.homo").addScanners(Scanners.TypesAnnotated));
        Set<Class<?>> set = reflections.getTypesAnnotatedWith(DirtyLandingServer.class);
        if (set.isEmpty()){
            return new String[0];
        }
        String[] typeClazz = new String[1];
        typeClazz[0] = DirtyAutoConfiguration.class.getName();
        return typeClazz;
    }

}
