package com.homo.core.landing.pojo;

import com.homo.core.mysql.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

@Builder
@Data
@AllArgsConstructor
public class DataObject {
    @TableField(value = "primary_key", type = JdbcType.VARCHAR)
    private String primaryKey;

    @TableField(value = "logic_type", type = JdbcType.INTEGER)
    private Integer logicType;

    @TableField(value = "owner_id", type = JdbcType.VARCHAR)
    private String ownerId;

    @TableField(type = JdbcType.VARCHAR)
    private String key;

    @TableField(type = JdbcType.BLOB)
    private byte[] value;

    @TableField(value = "up_version", type = JdbcType.BIGINT)
    private Long upVersion;

    @TableField(value = "is_del", type = JdbcType.INTEGER)
    private Integer isDel;

    @TableField(value = "del_time", type = JdbcType.BIGINT)
    private Long delTime;

    @TableField(value = "query_all_key", type = JdbcType.VARCHAR)
    private String queryAllKey;


    public static String buildTableName(String appId, String regionId) {
        return appId + ":" + regionId;
    }

    public static String buildQueryAllKey(String logicType, String ownerId) {
        return logicType + ":" + ownerId;
    }

    public static String buildPrimaryKey(String logicType, String ownerId, String field) {
        return logicType + ":" + ownerId + ":" + field;
    }


}
