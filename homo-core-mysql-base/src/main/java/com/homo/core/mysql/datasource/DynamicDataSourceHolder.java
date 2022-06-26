package com.homo.core.mysql.datasource;

/**
 * 动态数据源操作
  */
public class DynamicDataSourceHolder {

    private static final ThreadLocal<String> holder = new ThreadLocal<String>();

    private DynamicDataSourceHolder() {
    }

    public static void putDataSource(String key) {
        holder.set(key);
    }

    public static String getDataSource() {
        return holder.get();
    }

    public static void clearDataSource() {
        holder.remove();
    }

    public static boolean isMaster(){
        return holder.get().equals(DBType.MASTER);
    }


}
