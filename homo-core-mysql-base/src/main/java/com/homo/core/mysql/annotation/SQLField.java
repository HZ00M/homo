package com.homo.core.mysql.annotation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Field;

@Slf4j
public class SQLField {
    TableField tableField;
    Field field;
    Object value;
    String name;
    String type;
    String comment;
    String isIdField;
    String increment;
    String notNull;
    String defaultValue;

    private SQLField() {
    }

    public static class Builder {
        public static SQLField create(Field field, Object target) {
            SQLField sqlField = new SQLField();
            sqlField.field = field;
            if (target != null) {
                try {
                    sqlField.value = field.get(target);
                } catch (IllegalAccessException e) {
                    sqlField.value = "";
                    log.error("SQLField create error", e);
                }
            }
            TableField tableField = field.getAnnotation(TableField.class);
            sqlField.tableField = tableField;
            if (tableField != null) {
                sqlField.name = tableField.value().equals("") ? SQLUtil.humpToLine(field.getName()) : tableField.value();
                sqlField.type = tableField.type().equals(JdbcType.VARCHAR) ? tableField.type() + "(" + tableField.length() + ")" : tableField.type().name();
                sqlField.comment = tableField.comment();
                sqlField.notNull = tableField.notNull() ? " not null " : "";
                sqlField.isIdField = tableField.id() ? " primary key " : "";
                sqlField.increment = tableField.autoInr() ? " auto_increment = 1 " : "";
                sqlField.defaultValue = tableField.defaultValue();
            }
            return sqlField;
        }
    }

    public String toCreateString() {
        if (tableField == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder
                .append(" `")
                .append(name)
                .append("` ")
                .append(type)
                .append(" ")
                .append(StringUtils.isEmpty(defaultValue) ? "" : "DEFAULT " + defaultValue)
                .append(" ")
                .append(comment)
                .append(notNull)
                .append(isIdField)
                .append(increment)
                .append(",");
        return builder.toString();
    }

    public String toDuplicateString() {
        if (tableField == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder
                .append(" `")
                .append(name)
                .append("` ")
                .append("=VALUES(`")
                .append(name)
                .append("`),");
        return builder.toString();
    }

}
