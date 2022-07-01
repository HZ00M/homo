package com.homo.core.utils.serial;

/**
 * 序列化工具接口
 */
public interface HomoSerializationProcessor {
    /**
     * 序列化对象为字节
     * @param obj 对象
     * @return  字节
     */
    byte[] writeByte(Object obj);

    /**
     * 序列化对象为字符串
     * @param obj 对象
     * @return json字符串
     */
    String writeString(Object obj);

    /**
     * 反序列化字节为对象
     * @param obj 反序列化对象
     * @param clazz 对象类型信息
     * @param <T> 对象泛型信息
     * @return 反序列结果对象
     */
    <T> T readValue(byte[] obj,Class<T> clazz);

    /**
     * 反序列化字节为对象
     * @param obj 反序列化对象
     * @param reference 对象Type
     * @param <T> 对象泛型信息
     * @return 反序列化结果对象
     */
    <T> T readValue(byte[] obj,HomoTypeReference<T> reference);

    /**
     * 反序列化字节为对象
     * @param obj 反序列化对象
     * @param clazz 对象类型信息
     * @param <T> 对象泛型信息
     * @return 反序列结果对象
     */
    <T> T readValue(String obj,Class<T> clazz);

    /**
     * 反序列化字节为对象
     * @param obj 反序列化对象
     * @param reference 对象Type
     * @param <T> 对象泛型信息
     * @return 反序列化结果对象
     */
    <T> T readValue(String obj,HomoTypeReference reference);
}
