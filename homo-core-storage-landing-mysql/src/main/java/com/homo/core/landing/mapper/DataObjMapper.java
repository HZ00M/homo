package com.homo.core.landing.mapper;

import com.homo.core.facade.storege.landing.DataObjHelper;
import com.homo.core.mysql.GeneralMapper;
import com.homo.core.mysql.annotation.SQLGen;
import com.homo.core.mysql.entity.DataObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;


@Mapper
public interface DataObjMapper extends GeneralMapper {
    /**
     * 批量更新数据
     *
     * @param tableName        表名
     * @param list<DataObject> 指定结构
     * @return
     */
    @Transactional
    @InsertProvider(type = DataObjectGen.class, method = "batchUpdate")
    Integer batchUpdate(@Param("tableName") String tableName, @Param("list") List<DataObject> list);

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
    List<DataObject> loadDataObjectsByField(String appId, String regionId, String logicType, String ownerId, List<String> keys);

    @Slf4j
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
                    inBuilder.append(DataObjHelper.buildPrimaryKey(logicType, ownerId, key));
                    inBuilder.append("',");
                }
                inBuilder.deleteCharAt(inBuilder.length() - 1);
                inBuilder.append(")");
                WHERE("`primary_key`" + inBuilder.toString());
                SELECT(SEARCH_FIELDS);
            }}.toString();
        }
    }

    class DataObjectGen {
        static String INSERT_SQL_TMPL = "INSERT INTO `%s` (" +
                "`primary_key`," +
                "`logic_type`," +
                "`owner_id`," +
                "`key`," +
                "`value`," +
                "`up_version`," +
                "`is_del`, " +
                "`del_time`, " +
                "`query_all_key`, " +
                "`create_time`, " +
                "`update_time`" +
                ") VALUES %s";
        static String DUPLICATE_SQL = " ON DUPLICATE KEY UPDATE " +
                "`value` = VALUES(`value`), " +
                "`up_version` = VALUES(`up_version`), " +
                "`is_del` = VALUES(`is_del`)," +
                "`del_time` = VALUES(`del_time`), " +
                "`update_time` = VALUES(`update_time`)";

        public static String batchUpdate(@Param("tableName")String tableName,@Param("list")  List<DataObject> list) {
            StringBuilder sqlBuilder = new StringBuilder();

            MessageFormat messageFormat = new MessageFormat("({0}, #'{'list[{1}].logicType}," +
                    "#'{'list[{1}].ownerId},#'{'list[{1}].key},#'{'list[{1}].value}," +
                    "#'{'list[{1}].upVersion},#'{'list[{1}].isDel},#'{'list[{1}].delTime}, {2}, {3}, {4})");

            Long currentTime = System.currentTimeMillis();
            for (int i = 0; i < list.size(); i++) {
                DataObject dataObject = list.get(i);
                String primaryKey = dataObject.getPrimaryKey();

                String queryAllKey = dataObject.getQueryAllKey();
                sqlBuilder.append(messageFormat.format(new Object[]{"'" + primaryKey + "'", i,
                        "'" + queryAllKey + "'", currentTime + "", currentTime + ""}));
                if (i < list.size() - 1) {
                    sqlBuilder.append(",");
                }
            }
            String sql = String.format(INSERT_SQL_TMPL,
                    tableName, sqlBuilder.toString()) + DUPLICATE_SQL;
            return sql;
        }
    }
}
