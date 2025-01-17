package com.homo.core.facade.ability;

import com.homo.core.utils.fun.Func2PWithException;
import com.homo.core.utils.rector.Homo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * ability对象管理器
 */
public interface AbilityEntityMgr {

    /**
     * 添加entity
     *
     * @param abilityEntity entity派生类
     * @param <T>
     * @return
     */
    <T extends AbilityEntity> boolean add(T abilityEntity);

    /**
     * 获取entity
     *
     * @param type entityType
     * @param <T>
     * @return
     */
    <T extends AbilityEntity> T get(String type, String id);

    /**
     * 获取entity
     *
     * @param abilityClazz abilityClazz
     * @param <T>
     * @return
     */
    <T extends AbilityEntity> T get(Class<T> abilityClazz, String id);

    /**
     * 获取指定type的所有entity
     *
     * @param type entityType
     * @param <T>
     * @return
     */
    <T extends AbilityEntity> Set<T> getAll(String type);

    /**
     * 删除entity
     * 只是从内存中移除，并不是删除
     *
     * @param abilityEntity entity派生类
     * @param <T>
     * @return
     */
    default <T extends AbilityEntity> T remove(T abilityEntity) {
        return remove(abilityEntity.getType(), abilityEntity.getId());
    }

    /**
     * 删除entity
     * 只是从内存中移除，并不是删除
     *
     * @param type entityType
     * @param id   entityId
     * @param <T>
     * @return
     */
    <T extends AbilityEntity> T remove(String type, String id);

    /**
     * 删除entity
     * 只是从内存中移除，并不是删除
     *
     * @param abilityClazz entityClazz
     * @param id           entityId
     * @param <T>
     * @return
     */
    <T extends AbilityEntity> T remove(Class<T> abilityClazz, String id);

    /**
     * Destroy所有type指定id的entity，
     * 只是从内存中移除，并不是storage删除
     *
     * @param id
     */
    Homo<Boolean> removeAllType(String id);

    /**
     * 获取或创建对象
     *
     * @param id           entityId
     * @param abilityClazz entityClazz
     * @param params       构造参数
     * @param <T>
     * @return
     */
    <T extends AbilityEntity> Homo<T> getOrCreateEntityPromise(Class<T> abilityClazz, String id, Object... params);

    /**
     * 创建一个对象
     * 请在setEntityNotFoundProcessFun方法中设置回调调用此方法,否则可能有并发问题.
     *
     * @param id           entityId
     * @param abilityClazz entityClazz
     * @param params       entity构造函数参数
     * @param <T>          entity类型
     * @return 被创建的对象
     */
    <T extends AbilityEntity> Homo<T> createEntityPromise(Class<T> abilityClazz, String id, Object... params);

    /**
     * 获取或创建对象
     *
     * @param id   entityId
     * @param type entityType
     * @param <T>
     * @return
     */
    <T extends AbilityEntity> Homo<T> getEntityPromise(String type, String id);

    /**
     * 获取对象
     *
     * @param id           entityId
     * @param abilityClazz entityClazz
     * @param <T>
     * @return
     */
    <T extends AbilityEntity> Homo<T> getEntityPromise(Class<T> abilityClazz, String id);

    void removeAllEntity();

    /**
     * 注册一个OBJ创建消费函数
     *
     * @param clazz    注册key
     * @param consumer 消费函数
     */
    void registerCreateProcess(Class<?> clazz, Consumer<AbilityEntity> consumer);

    /**
     * 注册一个OBJ添加消费函数
     *
     * @param clazz    注册key
     * @param consumer 消费函数
     */
    void registerAddProcess(Class<?> clazz, Consumer<AbilityEntity> consumer);

    /**
     * 注册一个OBJ获取消费函数
     *
     * @param clazz    注册key
     * @param consumer 消费函数
     */
    void registerGetProcess(Class<?> clazz, Consumer<AbilityEntity> consumer);

    /**
     * 注册一个OBJ删除消费函数
     *
     * @param clazz    注册key
     * @param consumer 消费函数
     */
    void registerRemoveProcess(Class<?> clazz, Consumer<AbilityEntity> consumer);

    Map<Class<? extends AbilityEntity>, Func2PWithException<Class<? extends AbilityEntity>, String, Homo<? extends AbilityEntity>>> notFoundCreateFunMap = new HashMap<>();

    default void registerEntityNotFoundProcess(Class<? extends AbilityEntity> abilityEntity, Func2PWithException<Class<? extends AbilityEntity>, String, Homo<? extends AbilityEntity>> createFun) {
        notFoundCreateFunMap.put(abilityEntity, createFun);
    }
}
