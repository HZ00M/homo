package com.homo.core.enetity.util;

/**
 * 数据库 KEY 管理
 * @author aisen
 */
public final class Key {
    private final static String COLLECTION_FORMAT = "col_%s_%s";        //col_appId_regionId
    private final static String PRIMARY_FORMAT = "primary_%s_%s_%s";    //primary_logic_ownerId_key
    private final static String QUERY_ALL_FORMAT = "query_%s_%s";       //query_logic_ownerId

    public static final String PRIMARY_KEY = "primaryKey";
    public static final String DELETE_KEY = "isDel";
    public static final String KEY_KEY = "key";
    public static final String VALUE_KEY = "value";
    public static final String QUERY_ALL_KEY = "queryAllKey";

    public static final int DELETED_TRUE = 1;
    public static final int DELETED_FALSE = 0;

    public static String getColName(String appId,String regionId){
        return String.format(COLLECTION_FORMAT,appId,regionId);
    }

    public static String getQueryAllValue(String logicType, String ownerId){
        return String.format(QUERY_ALL_FORMAT,logicType,ownerId );
    }

    public static String getPrimaryValue(String logicType, String ownerId, String key){
        return String.format(PRIMARY_FORMAT,logicType,ownerId,key);
    }
}
