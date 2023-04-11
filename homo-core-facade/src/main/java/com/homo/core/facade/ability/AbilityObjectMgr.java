package com.homo.core.facade.ability;

import com.homo.core.utils.rector.Homo;

import java.util.Set;
import java.util.function.Consumer;

/**
 *  ability对象管理器
 */
public interface AbilityObjectMgr {

    /**
     * 添加entity
     * @param abilityEntity entity派生类
     * @return
     * @param <T>
     */
    <T extends AbilityObject> boolean add(T abilityEntity);

    /**
     * 获取entity
     * @param abilityEntity entity派生类
     * @return
     * @param <T>
     */
    <T extends AbilityObject> T get(T abilityEntity);

    /**
     * 获取指定type的所有entity
     * @param type entityType
     * @return
     * @param <T>
     */
    <T extends AbilityObject> Set<T> getAll(String type);

    /**
     * 删除entity
     * 只是从内存中移除，并不是删除
     * @param abilityEntity entity派生类
     * @return
     * @param <T>
     */
    <T extends AbilityObject> T remove(T abilityEntity);

    /**
     * 删除entity
     * 只是从内存中移除，并不是删除
     * @param type entityType
     * @param id entityId
     * @return
     * @param <T>
     */
    <T extends AbilityObject> T remove(String type,String id);

    /**
     * 删除entity
     * 只是从内存中移除，并不是删除
     * @param abilityClazz entityClazz
     * @param id entityId
     * @return
     * @param <T>
     */
    default <T extends AbilityObject> T remove(Class<T> abilityClazz, String id) {
        return remove(abilityClazz.getSimpleName(),id);
    }

    /**
     * Destroy所有type指定id的entity，
     * 只是从内存中移除，并不是storage删除
     * @param id
     */
    Homo<Boolean> removeAllType(String id);

    /**
     * 获取或创建对象
     * @param id entityId
     * @param abilityClazz entityClazz
     * @param params 构造参数
     * @return
     * @param <T>
     */
    <T extends AbilityObject> Homo<T> getOrCreateEntityPromise(String id, Class<T> abilityClazz, Object... params);

    /**
     * 创建一个对象
     * 请在setEntityNotFoundProcessFun方法中设置回调调用此方法,否则可能有并发问题.
     * @param id entityId
     * @param abilityClazz entityClazz
     * @param params entity构造函数参数
     * @param <T> entity类型
     * @return 被创建的对象
     */
    <T extends AbilityObject> Homo<T> createEntityPromise(String id, Class<T> abilityClazz, Object...params);
    /**
     * 获取或创建对象
     * @param id entityId
     * @param type entityType
     * @return
     * @param <T>
     */
    <T extends AbilityObject> Homo<T> getEntityPromise(String id, String type);
    /**
     * 获取对象
     * @param id entityId
     * @param abilityClazz entityClazz
     * @return
     * @param <T>
     */
    default <T extends AbilityObject> Homo<T> getEntityPromise(String id, Class<T> abilityClazz){
        return getEntityPromise(id,abilityClazz.getSimpleName());
    }

    void removeAllEntity();

    /**
     * 注册一个OBJ创建消费函数
     * @param clazz 注册key
     * @param consumer 消费函数
     */
    void registerCreateProcess(Class<?> clazz, Consumer<AbilityObject> consumer);

    /**
     * 注册一个OBJ添加消费函数
     * @param clazz 注册key
     * @param consumer 消费函数
     */
    void registerAddProcess(Class<?> clazz, Consumer<AbilityObject> consumer);
    /**
     * 注册一个OBJ获取消费函数
     * @param clazz 注册key
     * @param consumer 消费函数
     */
    void registerGetProcess(Class<?> clazz, Consumer<AbilityObject> consumer);
    /**
     * 注册一个OBJ删除消费函数
     * @param clazz 注册key
     * @param consumer 消费函数
     */
    void registerRemoveProcess(Class<?> clazz, Consumer<AbilityObject> consumer);

}
