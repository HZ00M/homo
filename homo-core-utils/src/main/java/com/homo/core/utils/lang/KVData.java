package com.homo.core.utils.lang;

import com.alibaba.fastjson.JSONObject;

/**
 * 存储KV形式的数据
 */
public class KVData {
    public JSONObject data;

    /**
     * 构造函数
     */
    public KVData() {
        reset();
    }

    public void reset() {
        data = new JSONObject();
    }

    public Object get(String key) {
        return data.get(key);
    }

    public JSONObject toJson(){
        return data;
    }

    public static KVData parseJson(String jsonStr){
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        KVData kvData = new KVData();
        kvData.data = jsonObject;
        return kvData;
    }

    public <T> T get(String key, Class<T> tClass) {
        return data.getObject(key, tClass);
    }

    public Integer getInt(String key) {
        return data.getInteger(key);
    }

    public Integer getInt(String key, int defaultValue) {
        Integer integer = getInt(key);
        return integer == null ? defaultValue : integer;
    }

    public Long getLong(String key) {
        return data.getLong(key);
    }

    public Long getLong(String key, long defaultValue) {
        Long aLong = getLong(key);
        return aLong == null ? defaultValue : aLong;
    }

    public String getString(String key) {
        return data.getString(key);
    }

    public String getString(String key, String defaultValue) {
        String string = getString(key);
        return string == null ? defaultValue : string;
    }

    public KVData add(String key,Integer value){
        data.put(key,value);
        return this;
    }

    public KVData add(String key,Long value){
        data.put(key,value);
        return this;
    }

    public KVData add(String key,String value){
        data.put(key,value);
        return this;
    }

    public <T> T set(String key,T value){
        Object rel = data.put(key, value);
        //由于fastJson会自动失败类型，所以当value为long时，原来的值可能是Integer
        if (rel instanceof Integer){
            if (value instanceof Long){
                rel = Long.valueOf((Integer) rel);
            }
        }
        return (T)rel;
    }
}
