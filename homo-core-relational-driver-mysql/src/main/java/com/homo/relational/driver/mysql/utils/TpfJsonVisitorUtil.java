package com.homo.relational.driver.mysql.utils;

public class TpfJsonVisitorUtil {
    public static void visit(StringBuilder from, StringBuilder where, StringBuilder builder) {

        if (where.toString().contains("json_extract")) {
            String[] split = where.toString().split("AND");
            for (int i = 0; i < split.length; i++) {
                if (split[i].contains("json_extract")) {
                    split[i] = split[i].replace(from + ".", "");
                    split[i] = split[i].replace("`", "");
                }
            }
            String replacedSql = String.join("AND", split);
            builder.append(" WHERE ").append(replacedSql);
        }else {
            builder.append(" WHERE ").append(where);
        }

    }
}
