package com.homo.core.utils.origin.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.homo.core.utils.origin.ExceptionUtil;
import com.homo.core.utils.origin.date.DatePattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;

public class JacksonMapper {

    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public JacksonMapper() {
        final JsonFactory jsonFactory = new JsonFactoryBuilder()
                .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
                .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
                .configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true)
                .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)
                .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS, true)
                .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
                .configure(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                .build();
        objectMapper= new ObjectMapper(jsonFactory);
        init();
    }
    public JacksonMapper(final JsonFactory jsonFactory) {
        objectMapper= new ObjectMapper(jsonFactory);
        init();
    }
    private void init(){
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        //通过该方法对mapper对象进行设置，所有序列化的对象都将按改规则进行系列化
        //JsonInclude.Include.ALWAYS 默认
        //JsonInclude.Include.NON_DEFAULT 属性为默认值不序列化
        //JsonInclude.Include.NON_EMPTY 属性为 空（“”） 或者为 NULL 都不序列化
        //JsonInclude.Include.NON_NULL 属性为NULL 不序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        /**
         * 不关闭WRITE_DATES_AS_TIMESTAMPS， java.util.Date序列化默认返回时间戳
         */
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        /**
         * 开启这些序列化配置。LocalDateTime,LocalDate,LocalTime会返回对应字符串格式
         */
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DatePattern.NORM_DATETIME_FORMATTER));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DatePattern.NORM_DATE_FORMATTER));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DatePattern.NORM_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DatePattern.NORM_DATETIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DatePattern.NORM_DATE_FORMATTER));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DatePattern.NORM_TIME_FORMATTER));
        objectMapper.registerModule(javaTimeModule);
    }


    public   String toJSONString(Object object, boolean throwJsonException) {
        String result = null;
        try {
            result = objectMapper.writeValueAsString(object);

        } catch (Throwable throwable) {
            if (throwJsonException) {
                ExceptionUtil.wrapAndThrow(throwable);
            } else {
                throwable.printStackTrace();
            }
        }
        return result;
    }

    public   String toJSONString(Object object) {
        return toJSONString(object, false);
    }


    public   <T> T parseObject(String jsonStr, Class<T> clazz, boolean throwJsonException) {
        T result = null;
        try {
            result = objectMapper.readValue(jsonStr, clazz);
        } catch (Throwable throwable) {
            if (throwJsonException) {
                ExceptionUtil.wrapAndThrow(throwable);
            } else {
                throwable.printStackTrace();
            }
        }
        return result;
    }

    public   <T> T parseObject(String jsonStr, Class<T> clazz) {
        return parseObject(jsonStr, clazz, false);
    }

    public   <T> T parseObject(String jsonStr, TypeReference<T> typeReference, boolean throwJsonException) {
        T result = null;
        try {
            result = objectMapper.readValue(jsonStr, typeReference);
        } catch (Throwable throwable) {
            if (throwJsonException) {
                ExceptionUtil.wrapAndThrow(throwable);
            } else {
                throwable.printStackTrace();
            }
        }
        return result;
    }

    public   <T> T parseObject(String jsonStr, TypeReference<T> typeReference) {
        return parseObject(jsonStr, typeReference, false);
    }

    public   <T> T parseObject(String jsonStr, JavaType javaType, boolean throwJsonException) {
        T result = null;
        try {
            result = objectMapper.readValue(jsonStr, javaType);

        } catch (Throwable throwable) {
            if (throwJsonException) {
                ExceptionUtil.wrapAndThrow(throwable);
            } else {
                throwable.printStackTrace();
            }
        }
        return result;
    }

    public   <T> T parseObject(String jsonStr, JavaType javaType) {
        return parseObject(jsonStr, javaType, false);
    }
}
