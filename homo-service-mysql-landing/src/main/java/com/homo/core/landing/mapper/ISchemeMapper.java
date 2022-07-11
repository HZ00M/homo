package com.homo.core.landing.mapper;

import com.homo.core.facade.storege.landing.DataObjHelper;
import com.homo.core.mysql.entity.DataObject;
import com.homo.core.mysql.GeneralMapper;
import com.homo.core.mysql.annotation.SQLGen;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

;

@Mapper
public interface ISchemeMapper extends GeneralMapper {

    /**
     * 捞取用户所有field的数据
     *
     * @param appId
     * @param regionId
     * @param logicType
     * @param ownerId
     * @return
     */
    @Transactional
    @SelectProvider(type = DataObjectProvider.class, method = "loadTableData")
     List<DataObject> loadAllDataObject(String appId, String regionId, String logicType, String ownerId);

    /**
     * 捞取用户指定field的数据
     *
     * @param appId
     * @param regionId
     * @param logicType
     * @param ownerId
     * @param keys
     * @return
     */
    @Transactional
    @SelectProvider(type = DataObjectProvider.class, method = "loadDataObjectsByField")
    List<DataObject>  loadDataObjectsByField(String appId, String regionId, String logicType, String ownerId, List<String> keys);

    @Log4j2
    class DataObjectProvider<T> {
        static String SEARCH_FIELDS = "`logic_type` as `logicType`,`owner_id` as `ownerId`, `key` as `key`,`value` as `value`";

        public <T> String loadTableData(String appId, String regionId, String logicType, String ownerId) {
            String queryAllKey = DataObject.buildQueryAllKey(logicType, ownerId);
            DataObject queryObj = DataObject.builder().queryAllKey(queryAllKey).isDel(0).build();
            String tableName = DataObject.buildTableName(appId, regionId);
            String sql = SQLGen.select(queryObj, tableName);
            return sql;
        }

        public String loadDataObjectsByField(String appId, String regionId, String logicType, String ownerId, List<String> keys) {
            return new SQL() {{
                String tableName = DataObject.buildTableName(appId, regionId);
                FROM(tableName);
                WHERE("`is_del`= 0 ");
                StringBuilder inBuilder = new StringBuilder("IN (");

                for (String key : keys) {
                    inBuilder.append("'");
                    inBuilder.append(DataObjHelper.buildPrimaryKey(logicType,ownerId,key));
                    inBuilder.append("',");
                }
                inBuilder.deleteCharAt(inBuilder.length() - 1);
                inBuilder.append(")");
                WHERE("`primary_key`" + inBuilder.toString());
                SELECT(SEARCH_FIELDS);
            }}.toString();
        }
    }

}
