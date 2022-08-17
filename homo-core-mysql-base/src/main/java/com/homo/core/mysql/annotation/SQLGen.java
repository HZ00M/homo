package com.homo.core.mysql.annotation;

import org.apache.ibatis.jdbc.SQL;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 封装CRUD
 * 在调用Mapper方法时传入相应的实体, 如果字段类型为String且包含%, 将使用like 进行查询, 该操作仅对select和delete操作有效.
 * insert,update则不受此限制, '%'百分号将作为内容被保存进数据库
 */
public class SQLGen<T> {

    public static <T> String create(Class<T> entity, String tableName) {
        String finalTableName = entity.getSimpleName();
        if (!StringUtils.isEmpty(tableName)) {
            finalTableName = tableName;
        } else {
            TableName annoTableName = AnnotationUtils.findAnnotation(entity, TableName.class);
            if (annoTableName != null) {
                finalTableName = annoTableName.value();
            }
        }
        StringBuilder sqlBuilder = new StringBuilder();

        List<SQLField> fieldList = new ArrayList<>();
        try {
            Field[] fields = entity.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                fieldList.add(SQLField.Builder.create(field, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (SQLField sqlField : fieldList) {
            sqlBuilder.append(sqlField.toCreateString());
        }
        String fieldStr = sqlBuilder.substring(0, sqlBuilder.length() - 1);
        String sql = String.format("CREATE TABLE IF NOT EXISTS `%s`( %s )ENGINE=InnoDB DEFAULT CHARSET=utf8", finalTableName, fieldStr);
        return sql;
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
            } else {
                TableName annoTableName = AnnotationUtils.findAnnotation(obj.getClass(), TableName.class);
                if (annoTableName != null) {
                    finalTableName = annoTableName.value();
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
                    String entityFieldName = field.getName();
                    String colName = SQLUtil.humpToLine(entityFieldName);
                    TableField tableField = AnnotationUtils.findAnnotation(field, TableField.class);
                    if (tableField == null) {
                        continue;
                    }
                    if (!StringUtils.isEmpty(tableField.value())) {
                        colName = tableField.value();
                    }
                    if (v != null) {
                        if (v instanceof String && ((String) v).contains("%")) {
                            WHERE(colName + " like '" + v + "'");
                        } else {
                            WHERE(colName + "='" + v + "'");
                        }
                    }
                    searchFileds.add(" `" + colName + "` as `" + entityFieldName + "` ");
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
            } else {
                TableName annoTableName = AnnotationUtils.findAnnotation(obj.getClass(), TableName.class);
                if (annoTableName != null) {
                    finalTableName = annoTableName.value();
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
                    Id id = AnnotationUtils.findAnnotation(field, Id.class);
                    if (id != null) {
                        fieldId = id.value();
                        entityId = field.getName();
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
            } else {
                TableName annoTableName = AnnotationUtils.findAnnotation(obj.getClass(), TableName.class);
                if (annoTableName != null) {
                    finalTableName = annoTableName.value();
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
                        String colName = SQLUtil.humpToLine(field.getName());
                        TableField tableField = AnnotationUtils.findAnnotation(field, TableField.class);
                        if (tableField != null && !StringUtils.isEmpty(tableField.value())) {
                            colName = tableField.value();
                        }
                        VALUES("`" + colName + "`", "'" + v + "'");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }}.toString();
    }

    public static <T> String delete(Class<T> entity, String tableName) {
        return new SQL() {{
            String finalTableName = entity.getSimpleName();
            if (!StringUtils.isEmpty(tableName)) {
                finalTableName = tableName;
            } else {
                TableName annoTableName = AnnotationUtils.findAnnotation(entity, TableName.class);
                if (annoTableName != null) {
                    finalTableName = annoTableName.value();
                }
            }
            DELETE_FROM(finalTableName);
            try {
                Field[] fields = entity.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object v = field.get(entity);
                    if (v != null) {
                        //进行字段映射处理
                        String entityFieldName = field.getName();
                        String colName = SQLUtil.humpToLine(entityFieldName);
                        TableField tableField = AnnotationUtils.findAnnotation(field, TableField.class);
                        if (tableField != null && !StringUtils.isEmpty(tableField.value())) {
                            colName = tableField.value();
                        }
                        if (v instanceof String && ((String) v).contains("%")) {
                            WHERE(colName + " like '" + v + "'");
                        } else {
                            WHERE(colName + "= '" + entityFieldName + "'");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }}.toString();
    }


}

