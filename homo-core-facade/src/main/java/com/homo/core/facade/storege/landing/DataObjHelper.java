package com.homo.core.facade.storege.landing;

import com.homo.core.facade.storege.dirty.DirtyHelper;

public class DataObjHelper extends DirtyHelper {

    public static  String DATA_DELIMITER = ":";

    public static String[] splitDataKey(String key) {
        return key.split(DATA_DELIMITER);
    }

    public static String buildTableName(String queryKey) {
        String[] segment = splitDirtyKey(queryKey);
        return buildTableName(segment[0],segment[1],segment[2],segment[3]);
    }

    public static String buildTableName(String appId, String regionId, String logicType, String ownerId){
        return String.format(REDIS_KEY_TMPL,appId,regionId,logicType,ownerId);
    }

    public static String buildPrimaryKey(Integer logicType, String ownerId, String key) {
        return logicType+ DATA_DELIMITER +ownerId+ DATA_DELIMITER +key;
    }

    public static String buildQueryAllKey(Integer logicType, String ownerId) {
        return logicType+ DATA_DELIMITER +ownerId;
    }

    public static String buildTableName(String appId, String regionId) {
        return String.format(MYSQL_TABLE_TMPL,dirtyProperties.getTablePrefix(),appId,regionId);
    }


}
