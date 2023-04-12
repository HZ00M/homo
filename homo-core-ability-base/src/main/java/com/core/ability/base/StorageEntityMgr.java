package com.core.ability.base;

import com.homo.core.configurable.ability.AbilityProperties;
import com.homo.core.facade.ability.AbilityEntity;
import com.homo.core.facade.ability.AbilitySystem;
import com.homo.core.facade.ability.EntityType;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class StorageEntityMgr extends CacheEntityMgr{
    @Autowired
    AbilityProperties abilityProperties;
    Map<Class<? extends AbilitySystem>, AbilitySystem> systemMap = new HashMap<>();
    static Map<String, Class<AbilityEntity>> typeToAbilityObjectClazzMap = new ConcurrentHashMap<>();
    @Autowired
    StorageEntityMgr(Set<? extends AbilitySystem> abilitySystems) {
        init(abilitySystems);
    }

    protected void init(Set<? extends AbilitySystem> abilitySystems) {
        for (AbilitySystem system : abilitySystems) {
            system.init(this);
            systemMap.put(system.getClass(),system);
        }
        String entityScanPath = abilityProperties.getEntityScanPath();
        Reflections reflections = new Reflections(entityScanPath);
        Set<Class<?>> entityClazzSet = reflections.getTypesAnnotatedWith(EntityType.class);
        for (Class<?> entityClazz : entityClazzSet) {
            EntityType entityType = entityClazz.getAnnotation(EntityType.class);
            String type = entityType.type();
            typeToAbilityObjectClazzMap.computeIfAbsent(type, k -> (Class<AbilityEntity>) entityClazz);
            typeToAbilityObjectClazzMap.computeIfPresent(type, (k, oldClazz) -> {
                if (!entityClazz.equals(oldClazz) && oldClazz.isAssignableFrom(entityClazz)) {//新的class是老的class的子类，代替老的class
                    return (Class<AbilityEntity>) entityClazz;
                } else {
                    return oldClazz;
                }
            });
        }
    }
}
