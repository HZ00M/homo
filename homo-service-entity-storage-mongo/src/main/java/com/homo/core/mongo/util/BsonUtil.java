package com.homo.core.mongo.util;

import com.alibaba.fastjson.JSON;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Transient;
import java.lang.reflect.*;
import java.util.*;

/**
 * mongo数据库转换工具
 */
public class BsonUtil {

    private static Logger logger = LoggerFactory.getLogger(BsonUtil.class);

    public static <T> List<T> toBeans(List<Document> documents, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        for (int i = 0; null != documents && i < documents.size(); i++) {
            list.add(toBean(documents.get(i), clazz));
        }
        return list;
    }

    @SuppressWarnings({"rawtypes", "unused", "unchecked"})
    public static <T> T toBean(Document document, Class<T> clazz) {
        String docJson = document.toJson();
        try {
            return JSON.parseObject(docJson, clazz);
        }catch (Exception e) {
            logger.error("toBean() error , clazz:" + clazz.getName(), e);
        }

        T entity = null;
        try {
            entity = (T) clazz.newInstance();

            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;//静态成员不转换
                }
                field.setAccessible(true);
                Class fieldClazz = field.getType();
                String key = field.getName();

                if ("id".equals(key)) {
                    key = "_id";
                }
                Object value = null;
                try {
                    value = field.get(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Object val = document.get(key);
                if (val == null) {
                    continue;
                }
                if (isPrimitive(fieldClazz) || fieldClazz == String.class) {
                    if (fieldClazz.isInstance(val)) {
                        field.set(entity, val);
                    } else {
                        logger.warn("Class {} field {} type {} misMatch to dbType {}", clazz.getSimpleName(), field.getName(), field.getGenericType(), val.getClass().getTypeName());
                    }
                    continue;
                }
                //数组
                if (fieldClazz.isArray()) {
                    String itemClazzName = fieldClazz.getTypeName().substring(0, fieldClazz.getTypeName().length() - 2);

                    Class itemClazz;
                    try {
                        itemClazz = Class.forName(itemClazzName);
                    } catch (ClassNotFoundException e) {
                        //此时为基本类型
                        itemClazz = toPrimitiveClass(itemClazzName);
                    }
                    Object array = toArray(document.get(key), itemClazz);
                    field.set(entity, array);
                    continue;
                }
                //列表
                if (List.class.isAssignableFrom(fieldClazz)) {
                    ParameterizedType fc = (ParameterizedType) field.getGenericType();
                    TypeVariable[] types = fieldClazz.getTypeParameters();
                    List list = (List) value;
                    if (value == null) {
                        list = new ArrayList<>();
                        field.set(entity, list);
                    }
                    toList(document.get(key), list, (Class) fc.getActualTypeArguments()[0]);
                    continue;
                }
                //哈希表
                if (Map.class.isAssignableFrom(fieldClazz)) {
                    ParameterizedType fc = (ParameterizedType) field.getGenericType();
                    Map map = (Map) value;
                    if (value == null) {
                        map = new HashMap<>();
                        field.set(entity, map);
                    }
                    toMap(document.get(key), map, (Class) fc.getActualTypeArguments()[0], (Class) fc.getActualTypeArguments()[1]);
                    continue;
                }
                document.put(key, toBean((Document) val, fieldClazz));
            }
        } catch (Exception e) {
            logger.error("toBean() error , clazz:" + clazz.getName(), e);
        }


        return entity;
    }

    /**
     * 转换成Document
     *
     * @param entity
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Document toDocument(Object entity) {
        if (entity == null) {
            return null;
        }

        Document document = new Document();
        Class clazz = entity.getClass();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;//静态成员不转换
            }
            field.setAccessible(true);
            Class fieldClazz = field.getType();
            if (fieldClazz.getAnnotationsByType(Transient.class).length > 0) {
                //@Transient 标识的属性不进行转换
                continue;
            }

            String key = field.getName();
            if ("id".equals(key)) {
                key = "_id";
            }
            Object value = null;
            try {
                value = field.get(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (value == null) {
                continue;
            }
            try {
                if (isPrimitive(fieldClazz) || fieldClazz == String.class) {
                    document.put(key, value);
                    continue;
                }

                if (fieldClazz.isArray()) { //数组
                    String itemClazzName = fieldClazz.getTypeName().substring(0, fieldClazz.getTypeName().length() - 2);
                    Class itemClazz = null;
                    try {
                        itemClazz = Class.forName(itemClazzName);
                    } catch (ClassNotFoundException e) {
                        //此时为基本类型
                        itemClazz = toPrimitiveClass(itemClazzName);
                    }

                    int len = Array.getLength(value);

                    if (isPrimitive(itemClazz) || itemClazz == String.class) {
                        List values = new ArrayList<>();

                        for (int i = 0; i < len; i++) {
                            Object object = Array.get(value, i);
                            values.add(object);
                        }

                        document.put(key, values);
                    } else {
                        List<Document> listDocument = new ArrayList<>();
                        document.put(key, listDocument);

                        for (int i = 0; i < len; i++) {
                            Object object = Array.get(value, i);
                            listDocument.add(toDocument(object));
                        }
                    }
                    continue;
                }
                //列表
                if (List.class.isAssignableFrom(fieldClazz)) {
                    List list = (List) value;
                    ParameterizedType fc = (ParameterizedType) field.getGenericType();
                    Class itemClazz = (Class) fc.getActualTypeArguments()[0];

                    if (isPrimitive(itemClazz) || itemClazz == String.class) {
                        List values = new ArrayList<>();
                        for (Object object : list) {
                            values.add(object);
                        }
                        document.put(key, values);
                    } else {
                        List<Document> listDocument = new ArrayList<>();
                        document.put(key, listDocument);
                        for (Object object : list) {
                            listDocument.add(toDocument(object));
                        }
                    }
                    continue;
                }

                //哈希表
                if (Map.class.isAssignableFrom(fieldClazz)) {
                    Map map = (Map) field.get(entity);
                    Set<Map.Entry> entries = map.entrySet();
                    Map mpperMap = new HashMap<>();
                    document.put(key, mpperMap);

                    ParameterizedType fc = (ParameterizedType) field.getGenericType();
                    Class keyClazz = (Class) fc.getActualTypeArguments()[0];
                    if (keyClazz != String.class && !isPrimitive(keyClazz)) {
                        throw new RuntimeException("不支持的Map,转换成document的key只能为基本类型或字符串");
                    }
                    Class itemClazz = (Class) fc.getActualTypeArguments()[1];
                    if (itemClazz == String.class || isPrimitive(itemClazz)) {
                        for (Map.Entry entry : entries) {
                            mpperMap.put(entry.getKey().toString(), entry.getValue());
                        }
                    } else {
                        for (Map.Entry entry : entries) {
                            mpperMap.put(entry.getKey().toString(), toDocument(entry.getValue()));
                        }
                    }
                    continue;
                }
                document.put(key, toDocument(value));
            } catch (Exception e) {
                logger.error("toDocument() , error clazz=" + entity.getClass().getName(), e);
            }
        }
        return document;
    }

    @SuppressWarnings("rawtypes")
    public static boolean isPrimitive(Class clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        if (Long.class == clazz || Integer.class == clazz || Float.class == clazz || Double.class == clazz || Short.class == clazz || Boolean.class == clazz) {
            return true;
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object toArray(Object value, Class itemClazz) {
        List list = (List) value;

        Object array = Array.newInstance(itemClazz, list.size());
        int i = 0;
        for (Object object : list) {
            if (object instanceof Document) {
                Array.set(array, i++, toBean((Document) object, itemClazz));
            } else {
                Array.set(array, i++, object);
            }
        }
        return array;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void toMap(Object value, Map map, Class keyClazz, Class itemClazz) throws InstantiationException, IllegalAccessException {
        Set<Map.Entry> entries = ((Map) value).entrySet();
        for (Map.Entry entry : entries) {
            Object keyV = entry.getKey().getClass() == String.class ? entry.getKey() : toPrimitive(entry.getKey().toString(), keyClazz);
            Object itemV = entry.getValue();
            if (itemV instanceof Document) {
                map.put(keyV, toBean((Document) itemV, itemClazz));
            } else {
                map.put(keyV, itemV);
            }
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, Object> getUpdateMap(Object entity) {
        Map<String, Object> updateData = new HashMap<>(1);
        if (entity == null) {
            return updateData;
        }

        Class clazz = entity.getClass();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;//静态成员不转换
            }
            field.setAccessible(true);
            Class fieldClazz = field.getType();
            if (fieldClazz.getAnnotationsByType(Transient.class).length > 0) {
                //@Transient 标识的属性不进行转换
                continue;
            }

            String key = "value." + field.getName();
            Object value = null;
            try {
                value = field.get(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (value == null) {
                continue;
            }
            try {
                if (isPrimitive(fieldClazz) || fieldClazz == String.class) {
                    updateData.put(key, value);
                    continue;
                }

                if (fieldClazz.isArray()) { //数组
                    String itemClazzName = fieldClazz.getTypeName().substring(0, fieldClazz.getTypeName().length() - 2);
                    Class itemClazz = null;
                    try {
                        itemClazz = Class.forName(itemClazzName);
                    } catch (ClassNotFoundException e) {
                        //此时为基本类型
                        itemClazz = toPrimitiveClass(itemClazzName);
                    }

                    int len = Array.getLength(value);

                    if (isPrimitive(itemClazz) || itemClazz == String.class) {
                        List values = new ArrayList<>();

                        for (int i = 0; i < len; i++) {
                            Object object = Array.get(value, i);
                            values.add(object);
                        }

                        updateData.put(key, values);
                    } else {
                        List<Document> listDocument = new ArrayList<>();
                        updateData.put(key, listDocument);

                        for (int i = 0; i < len; i++) {
                            Object object = Array.get(value, i);
                            listDocument.add(toDocument(object));
                        }
                    }
                    continue;
                }
                //列表
                if (List.class.isAssignableFrom(fieldClazz)) {
                    List list = (List) value;
                    ParameterizedType fc = (ParameterizedType) field.getGenericType();
                    Class itemClazz = (Class) fc.getActualTypeArguments()[0];

                    if (isPrimitive(itemClazz) || itemClazz == String.class) {
                        List values = new ArrayList<>();
                        for (Object object : list) {
                            values.add(object);
                        }
                        updateData.put(key, values);
                    } else {
                        List<Document> listDocument = new ArrayList<>();
                        updateData.put(key, listDocument);
                        for (Object object : list) {
                            listDocument.add(toDocument(object));
                        }
                    }
                    continue;
                }

                //哈希表
                if (Map.class.isAssignableFrom(fieldClazz)) {
                    Map map = (Map) field.get(entity);
                    Set<Map.Entry> entries = map.entrySet();
                    Map mpperMap = new HashMap<>();
                    updateData.put(key, mpperMap);

                    ParameterizedType fc = (ParameterizedType) field.getGenericType();
                    Class keyClazz = (Class) fc.getActualTypeArguments()[0];
                    if (keyClazz != String.class && !isPrimitive(keyClazz)) {
                        throw new RuntimeException("不支持的Map,转换成document的key只能为基本类型或字符串");
                    }
                    Class itemClazz = (Class) fc.getActualTypeArguments()[1];
                    if (itemClazz == String.class || isPrimitive(itemClazz)) {
                        for (Map.Entry entry : entries) {
                            mpperMap.put(entry.getKey().toString(), entry.getValue());
                        }
                    } else {
                        for (Map.Entry entry : entries) {
                            mpperMap.put(entry.getKey().toString(), toDocument(entry.getValue()));
                        }
                    }
                    continue;
                }
                updateData.put(key, toDocument(value));
            } catch (Exception e) {
                logger.error("toUpdateData() , error clazz=" + entity.getClass().getName(), e);
            }
        }
        return updateData;
    }

    @SuppressWarnings("rawtypes")
    private static Object toPrimitive(String value, Class clazz) {
        if (int.class == clazz || Integer.class == clazz) {
            return Integer.valueOf(value);
        } else if (long.class == clazz || Long.class == clazz) {
            return Long.valueOf(value);
        } else if (short.class == clazz || Short.class == clazz) {
            return Short.valueOf(value);
        } else if (double.class == clazz || Double.class == clazz) {
            return Double.valueOf(value);
        } else if (float.class == clazz || Float.class == clazz) {
            return Float.valueOf(value);
        } else if (boolean.class == clazz || Boolean.class == clazz) {
            return Boolean.valueOf(value);
        } else {
            throw new RuntimeException("Map key nonsupport !!!");
        }
    }

    @SuppressWarnings("rawtypes")
    private static Class toPrimitiveClass(String primitiveClazzName) {
        Class itemClazz = null;
        //此时为基本类型
        if ("long".equals(primitiveClazzName)) {
            itemClazz = long.class;
        } else if ("int".equals(primitiveClazzName)) {
            itemClazz = int.class;
        } else if ("short".equals(primitiveClazzName)) {
            itemClazz = short.class;
        } else if ("double".equals(primitiveClazzName)) {
            itemClazz = double.class;
        } else if ("float".equals(primitiveClazzName)) {
            itemClazz = float.class;
        } else if ("boolean".equals(primitiveClazzName)) {
            itemClazz = boolean.class;
        } else {
            throw new RuntimeException("nonsupport type !!!");
        }
        return itemClazz;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void toList(Object value, List list, Class itemClazz) throws IllegalAccessException {
        List<Document> vals = (List) value;
        for (Document document : vals) {
            list.add(toBean(document, itemClazz));
        }
    }

}