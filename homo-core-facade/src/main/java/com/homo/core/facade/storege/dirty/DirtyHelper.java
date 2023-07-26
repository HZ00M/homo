package com.homo.core.facade.storege.dirty;

import com.homo.core.configurable.dirty.DirtyProperties;
import com.homo.core.facade.storege.DataOpType;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

;

@Log4j2
public class DirtyHelper {
    public static  String ERROR_SUFFIX = "errorFlag";
    public static  String DEL_SUFFIX = ":delFlag";
    public static  String DIRTY_DELIMITER = "&";
    public static final String REDIS_KEY_TMPL = "slug:{%s:%s:%s:%s}";

    public static DirtyProperties dirtyProperties;
    public static String APP_ID = "dirty1";
    public static String REGION_ID = "dirty2";
    public static String LOGIC_ID= "dirty3";
    public static String OWNER_ID= "dirty4";

    public String key;
    public Map<String, String> dirtyMap = new HashMap<>();

    public static void init(DirtyProperties dirtyProperties){
        DirtyHelper.dirtyProperties = dirtyProperties;
    }

    public static DirtyHelper create(String key) {
        DirtyHelper builder = new DirtyHelper();
        builder.key = key;
        return builder;
    }

    public DirtyHelper update(String appId, String regionId, String logicType, String ownerId, String field) {
        String dirtyKey = buildDirtyKey(appId, regionId, logicType, ownerId, field);
        dirtyMap.put(dirtyKey, DataOpType.UPDATE.name());
        return this;
    }

    public DirtyHelper incr(String appId, String regionId, String logicType, String ownerId, String field, Long inrValue) {
        String dirtyKey = buildDirtyKey(appId, regionId, logicType, ownerId, field);
        dirtyMap.put(dirtyKey, String.valueOf(inrValue));
        return this;
    }

    public DirtyHelper remove(String appId, String regionId, String logicType, String ownerId, String field) {
        String dirtyKey = buildDirtyKey(appId, regionId, logicType, ownerId, field);
        dirtyMap.put(dirtyKey, DataOpType.REMOVE.name());
        return this;
    }


    public DirtyImpl build() {
        Assert.isTrue(key != null && !dirtyMap.isEmpty(), "params miss !");
        return new DirtyImpl(key, dirtyMap);
    }

    public static String getSnapShotName(String dirtyName){
        return dirtyName+dirtyProperties.getSnapshotSuffix();
    }

    public static String getErrorName(String dirtyName){
        return dirtyName+dirtyProperties.getErrorSuffix();
    }

    public static String chooseDirtyMap(String key){
        int hashCode = key.hashCode();
        if (hashCode<0){
            hashCode = - hashCode;
        }
        return "slug-persist:{"+dirtyProperties.getTablePrefix()+":"+hashCode%dirtyProperties.getTableNum()+"}";
    }

    public static String chooseDirtyMap(Integer key){
        return "slug:{"+dirtyProperties.getTablePrefix()+":"+key+"}";
    }

    public static String buildDirtyKey(String queryKey, String field) {
        String[] segments = splitQueryKey(queryKey);
        return buildDirtyKey(segments[0],segments[1],segments[2],segments[3],field);
    }

    public static String buildDirtyKey(String appId, String regionId, String logicType, String ownerId, String field) {
        return appId + DIRTY_DELIMITER + regionId + DIRTY_DELIMITER + logicType + DIRTY_DELIMITER + ownerId + DIRTY_DELIMITER + field;
    }

    public static String buildErrorTableName(String dirtyTableName) {
        return dirtyTableName +ERROR_SUFFIX;
    }

    public static String[] splitQueryKey(String key) {
        return key.split(DIRTY_DELIMITER);
    }

    public static class DirtyImpl implements Dirty {
        public String key;
        public Map<String, String> dirtyMap;

        private DirtyImpl(String key, Map<String, String> dirtyMap) {
            this.key = key;
            this.dirtyMap = dirtyMap;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Map<String, String> dirtyMap() {
            return dirtyMap;
        }

    }

}
