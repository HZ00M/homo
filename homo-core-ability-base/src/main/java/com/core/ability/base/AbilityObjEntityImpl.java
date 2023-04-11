package com.core.ability.base;

import com.homo.core.facade.ability.Ability;
import com.homo.core.facade.ability.AbilityObject;
import com.homo.core.facade.ability.AbilityObjectMgr;
import com.homo.core.utils.lang.KKMap;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

@Component
@Slf4j
public class AbilityObjEntityImpl implements AbilityObjectMgr {
    private KKMap<String, String, AbilityObject> type2Id2ObjMap = new KKMap<>();

    @Override
    public <T extends AbilityObject> boolean add(T abilityEntity) {
        AbilityObject old = type2Id2ObjMap.get(abilityEntity.getType(), abilityEntity.getId());
        if (old != null) {
            
            ;
        }
        return false;
    }

    @Override
    public <T extends AbilityObject> T get(T abilityEntity) {
        return null;
    }

    @Override
    public <T extends AbilityObject> Set<T> getAll(String type) {
        return null;
    }

    @Override
    public <T extends AbilityObject> T remove(T abilityEntity) {
        return null;
    }

    @Override
    public <T extends AbilityObject> T remove(String type, String id) {
        return null;
    }

    @Override
    public Homo<Boolean> removeAllType(String id) {
        return null;
    }

    @Override
    public <T extends AbilityObject> Homo<T> getOrCreateEntityPromise(String id, Class<T> abilityClazz, Object... params) {
        return null;
    }

    @Override
    public <T extends AbilityObject> Homo<T> createEntityPromise(String id, Class<T> abilityClazz, Object... params) {
        return null;
    }

    @Override
    public <T extends AbilityObject> Homo<T> getEntityPromise(String id, String type) {
        return null;
    }

    @Override
    public void removeAllEntity() {

    }

    @Override
    public void registerCreateProcess(Class<?> clazz, Consumer<AbilityObject> consumer) {

    }

    @Override
    public void registerAddProcess(Class<?> clazz, Consumer<AbilityObject> consumer) {

    }

    @Override
    public void registerGetProcess(Class<?> clazz, Consumer<AbilityObject> consumer) {

    }

    @Override
    public void registerRemoveProcess(Class<?> clazz, Consumer<AbilityObject> consumer) {

    }
}
