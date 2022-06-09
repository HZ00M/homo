package com.homo.core.mysql.annotation;

import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 封装CRUD
 * 在调用Mapper方法时传入相应的实体, 如果字段类型为String且包含%, 将使用like 进行查询, 该操作仅对select和delete操作有效.
 * insert,update则不受此限制, '%'百分号将作为内容被保存进数据库
 */
public class SQLGen<T> {
    public static <T> String create(T obj, String tableName) {
        String finalTableName = obj.getClass().getSimpleName();
        if (!StringUtils.isEmpty(tableName)) {
            finalTableName = tableName;
        }
        Annotation[] annotations = obj.getClass().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof TableName) {
                finalTableName = ((TableName) annotation).value();
            }
        }
        List<SQLField> fieldList = new ArrayList<>();
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Annotation[] fieldAnnotions = field.getAnnotations();
                SQLField.SQLFieldBuilder builder = SQLField.builder();
                for (Annotation annotation : fieldAnnotions) {
                    if (annotation instanceof TableField) {
                        builder.name(((TableField) annotation).value());
                        builder.comment(((TableField) annotation).comment());
                        builder.type(((TableField) annotation).type());
                    }
                    if (annotation instanceof Id) {
                        builder.name(((Id) annotation).value());
                        builder.increment(((Id) annotation).autoIncr());
                        builder.isIdField(true);
                    }
                    fieldList.add(builder.build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder sqlBuilder = new StringBuilder();
        for (SQLField sqlField : fieldList) {
            sqlBuilder.append(sqlField.toCreateString());
        }
        sqlBuilder.append("CREATE TABLE IF NOT EXISTS ");
        sqlBuilder.append(finalTableName);
        sqlBuilder.append(sqlBuilder.substring(0, sqlBuilder.length() - 1));
        return sqlBuilder.toString();
    }

    public static <T> String drop(String tableName) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("DROP TABLE IF EXISTS ")
                .append(tableName);
        return sqlBuilder.toString();
    }

    public static <T> String select(T obj, String tableName) {
        return new SQL() {{
            String finalTableName = obj.getClass().getSimpleName();
            if (!StringUtils.isEmpty(tableName)) {
                finalTableName = tableName;
            }
            Annotation[] annotations = obj.getClass().getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof TableName) {
                    finalTableName = ((TableName) annotation).value();
                }
            }
            FROM(finalTableName);
            List<String> searchFileds = new ArrayList();
            try {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object v = field.get(obj);
                    //进行字段映射处理
                    String fieldName = SQLUtil.humpToLine(field.getName());
                    String entityFieldName = field.getName();

                    Annotation[] fieldAnnotions = field.getAnnotations();
                    for (Annotation annotation : fieldAnnotions) {
                        if (annotation instanceof TableField) {
                            fieldName = ((TableField) annotation).value();
                        }
                    }
                    if (v != null) {
                        if (v instanceof String && ((String) v).contains("%")) {
                            WHERE(fieldName + " like '" + v + "'");
                        } else {
                            WHERE(fieldName + "=#{" + entityFieldName + "}");
                        }
                    }
                    searchFileds.add(fieldName + " as " + entityFieldName);
                }
                SELECT(searchFileds.stream().collect(Collectors.joining(",")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }}.toString();
    }

    public static <T> String update(T obj, String tableName) {
        return new SQL() {{
            String finalTableName = obj.getClass().getSimpleName();
            if (!StringUtils.isEmpty(tableName)) {
                finalTableName = tableName;
            }
            Annotation[] annotations = obj.getClass().getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof TableName) {
                    finalTableName = ((TableName) annotation).value();
                }
            }
            UPDATE(finalTableName);
            String fieldId = "id";
            String entityId = "id";
            try {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object v = field.get(obj);
                    Annotation[] fieldAnnotions = field.getAnnotations();
                    for (Annotation annotation : fieldAnnotions) {
                        if (annotation instanceof Id) {
                            fieldId = ((Id) annotation).value();
                            entityId = field.getName();
                        }
                    }
                    if (v != null) {
                        String fieldName = SQLUtil.humpToLine(field.getName());
                        String entityName = field.getName();
                        SET(fieldName + "=#{" + entityName + "}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            WHERE(fieldId + "= #{" + entityId + "}");
        }}.toString();
    }

    public static <T> String insert(T obj, String tableName) {
        return new SQL() {{
            String finalTableName = obj.getClass().getSimpleName();
            if (!StringUtils.isEmpty(tableName)) {
                finalTableName = tableName;
            }
            Annotation[] annotations = obj.getClass().getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof TableName) {
                    finalTableName = ((TableName) annotation).value();
                }
            }
            INSERT_INTO(finalTableName);
            try {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object v = field.get(obj);
                    if (v != null) {
                        //进行字段映射处理
                        String fieldName = SQLUtil.humpToLine(field.getName());
                        String entityFieldName = field.getName();
                        Annotation[] fieldAnnotions = field.getAnnotations();
                        for (Annotation annotation : fieldAnnotions) {
                            if (annotation instanceof TableField) {
                                fieldName = ((TableField) annotation).value();
                            }
                        }
                        VALUES(fieldName, "#{" + entityFieldName + "}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }}.toString();
    }

    public static <T> String delete(T obj, String tableName) {
        return new SQL() {{
            String finalTableName = obj.getClass().getSimpleName();
            if (!StringUtils.isEmpty(tableName)) {
                finalTableName = tableName;
            }
            Annotation[] annotations = obj.getClass().getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof TableName) {
                    finalTableName = ((TableName) annotation).value();
                }
            }
            DELETE_FROM(finalTableName);
            try {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object v = field.get(obj);
                    if (v != null) {
                        //进行字段映射处理
                        String fieldName = SQLUtil.humpToLine(field.getName());
                        String entityFieldName = field.getName();
                        Annotation[] fieldAnnotions = field.getAnnotations();
                        for (Annotation annotation : fieldAnnotions) {
                            if (annotation instanceof TableField) {
                                fieldName = ((TableField) annotation).value();
                            }
                        }
                        if (v instanceof String && ((String) v).contains("%")) {
                            WHERE(fieldName + " like '" + v + "'");
                        } else {
                            WHERE(fieldName + "=#{" + entityFieldName + "}");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }}.toString();
    }


}

