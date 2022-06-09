package com.homo.core.persisten.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SchemeUtils {
    private SchemeUtils(){}
    static Map<String, Boolean> tableTags = new ConcurrentHashMap<>();

    public static boolean checkTableNotExist(String tableName){
        return !tableTags.containsKey(tableName);
    }

    public static void mark(String tableName) {
        tableTags.put(tableName, true);
    }

}
