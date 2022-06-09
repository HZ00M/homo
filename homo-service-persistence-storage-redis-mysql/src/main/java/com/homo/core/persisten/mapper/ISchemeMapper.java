package com.homo.core.persisten.mapper;

import com.homo.core.common.pojo.DataObject;
import com.homo.core.mysql.annotation.SQLGen;
import com.homo.core.mysql.annotation.SQLUtil;
import com.homo.core.mysql.annotation.TableField;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;


public interface ISchemeMapper {

    /**
     * 批量更新数据
     *
     * @param tableName        表名
     * @param list<DataObject> 指定结构
     * @return
     */
    @InsertProvider(type = DataObjectProvider.class, method = "batchUpdate")
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
    @SelectProvider(type = DataObjectProvider.class, method = "loadDataObjectsByField")
    List<DataObject> loadDataObjectsByField(String appId, String regionId, String logicType, String ownerId, List<String> keys);

    @Slf4j
    class DataObjectProvider<T> {
        static String DUPLICATE_SQL = " ON DUPLICATE KEY UPDATE ";
        static String SEARCH_FIELDS = "`logic_type`,`owner_id`, `key`,`value`";

        public String batchUpdate(String tableName, List<T> list) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append(DUPLICATE_SQL);

            String sql = new SQL() {{
                INSERT_INTO(tableName);
                for (T obj : list) {
                    try {
                        Field[] fields = obj.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            Object v = field.get(obj);
                            if (v != null) {
                                //进行字段映射处理
                                String fieldName = SQLUtil.humpToLine(field.getName());
                                Annotation[] fieldAnnotions = field.getAnnotations();
                                for (Annotation annotation : fieldAnnotions) {
                                    if (annotation instanceof TableField) {
                                        fieldName = ((TableField) annotation).value();
                                    }
                                }
                                VALUES(fieldName, "#{" + v + "}");
                                sqlBuilder.append(" `")
                                        .append(fieldName)
                                        .append("` ")
                                        .append("=VALUES(`")
                                        .append(fieldName)
                                        .append("`), ");
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }}.toString();
            String finalSql = sql + sqlBuilder.substring(0, sqlBuilder.length() - 1);
            return finalSql;
        }

        public String loadTableData(String appId, String regionId, String logicType, String ownerId) {
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
                    inBuilder.append(key);
                    inBuilder.append(",");
                }
                inBuilder.deleteCharAt(inBuilder.length() - 1);
                inBuilder.append(")");
                WHERE("`primary_key`" + inBuilder.toString());
                SELECT(SEARCH_FIELDS);
            }}.toString();
        }
    }

}
