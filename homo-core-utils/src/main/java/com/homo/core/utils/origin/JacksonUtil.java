package com.homo.core.utils.origin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.homo.core.utils.origin.json.JacksonMapper;

/**
 * Created by galfordliu on 2017/3/27.
 */
public class JacksonUtil {

    private final static JacksonMapper JACKSON_MAPPER = new JacksonMapper();


    private JacksonUtil() {

    }

    public static ObjectMapper getObjectMapper(){
        return JACKSON_MAPPER.getObjectMapper();
    }

    public static void setDebug(boolean debug){
        JACKSON_MAPPER.getObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, debug);
    }
    public static void setCaseInsensitive(boolean caseInsensitive){
        JACKSON_MAPPER.getObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,caseInsensitive);
    }
    public static String toJSONString(Object object, boolean throwJsonException) {
        return JACKSON_MAPPER.toJSONString(object,throwJsonException);
    }

    public static String toJSONString(Object object) {
        return toJSONString(object, false);
    }


    public static <T> T parseObject(String jsonStr, Class<T> clazz, boolean throwJsonException) {
        return JACKSON_MAPPER.parseObject(jsonStr,clazz,throwJsonException);
    }

    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        return parseObject(jsonStr, clazz, false);
    }

    public static <T> T parseObject(String jsonStr, TypeReference<T> typeReference, boolean throwJsonException) {
        return JACKSON_MAPPER.parseObject(jsonStr,typeReference,throwJsonException);
    }

    public static <T> T parseObject(String jsonStr, TypeReference<T> typeReference) {
        return parseObject(jsonStr, typeReference, false);
    }

    public static <T> T parseObject(String jsonStr, JavaType javaType, boolean throwJsonException) {
        return JACKSON_MAPPER.parseObject(jsonStr,javaType,throwJsonException);
    }

    public static <T> T parseObject(String jsonStr, JavaType javaType) {
        return parseObject(jsonStr, javaType, false);
    }
}
