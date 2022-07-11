package com.homo.core.mysql;


import com.homo.core.mysql.annotation.SQLGen;
import com.homo.core.mysql.entity.DataObject;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;

/**
 * 通用mapper
 *
 * @param <T>
 */
public interface GeneralMapper<T> {
    /**
     * 批量更新数据
     *
     * @param tableName        表名
     * @param list<DataObject> 指定结构
     * @return
     */
    @Transactional
    @InsertProvider(type = DataObjectGen.class, method = "batchUpdate")
    Integer batchUpdate(String tableName, List<DataObject> list);

    @SelectProvider(type = SQLGen.class, method = "create")
    void create(Class<T> t, String tableName);

    @SelectProvider(type = SQLGen.class, method = "drop")
    void drop(T t, String tableName);

    @InsertProvider(type = SQLGen.class, method = "insert")
    int add(T t, String tableName);

    @DeleteProvider(type = SQLGen.class, method = "delete")
    int del(T t, String tableName);

    @UpdateProvider(type = SQLGen.class, method = "update")
    int update(T t, String tableName);

    @SelectProvider(type = SQLGen.class, method = "select")
    List<T> select(T t, String tableName);

    class DataObjectGen{
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

        public static String batchUpdate(String tableName, List<DataObject> list) {
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
