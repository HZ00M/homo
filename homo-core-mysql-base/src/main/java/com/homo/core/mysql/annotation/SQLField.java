package com.homo.core.mysql.annotation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.ibatis.type.JdbcType;

@AllArgsConstructor
@Builder
public class SQLField {
    String name;
    JdbcType type;
    String comment;
    boolean isIdField;
    Integer increment;
    boolean isNotNull;

    public String toCreateString() {
        StringBuilder builder = new StringBuilder();
        if (isIdField) {
            builder
                    .append(" `")
                    .append(name)
                    .append("` ")
                    .append(type.toString())
                    .append(" ")
                    .append(comment)
                    .append(" auto_increment= ")
                    .append(increment)
                    .append(",");
        } else {
            builder = new StringBuilder()
                    .append(" `")
                    .append(name)
                    .append("` ")
                    .append(type.toString())
                    .append(comment);
            if (isNotNull) {
                builder.append(" not null ");
            }
            builder.append(",");

        }
        return builder.toString();
    }
}
