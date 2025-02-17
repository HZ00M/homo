package com.homo.core.configurable.relational;

import com.ctrip.framework.foundation.Foundation;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
@Slf4j
/**
 * MySQL 关系型数据库的配置信息
 */
public class RelationalMysqlProperties implements InitializingBean {
    /** MySQL 连接端口 */
    @Value("${homo.relational.mysql.connect.port:}")
    private int port;

    /** MySQL 连接主机地址 */
    @Value("${homo.relational.mysql.connect.host:}")
    private String host;

    /** 连接的数据库名称 */
    @Value("${homo.relational.mysql.connect.database:}")
    private String database;

    /**
     * 数据库驱动类型（默认值为 "mysql"）
     * 可用于区分不同数据库类型，如 MySQL、PostgreSQL 等。
     */
    @Value("${homo.relational.mysql.connect.driver:mysql}")
    private String driver;

    /**
     * MySQL 连接 URL
     * 示例:
     * - JDBC: jdbc:mysql://mysql:30006/tpf_storage?Unicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
     * - R2DBC: r2dbc:mysql://mysql:30006/tpf_storage?useSSL=false&characterEncoding=UTF-8
     */
    @Value("${homo.relational.mysql.connect.url:}")
    private String url;

    /** MySQL 连接用户名 */
    @Value("${homo.relational.mysql.connect.username:}")
    private String username;

    /** MySQL 连接密码 */
    @Value("${homo.relational.mysql.connect.password:}")
    private String password;

    /**
     * 连接超时时间（单位：秒，默认值 3）
     * 表示数据库连接的超时等待时间。
     */
    @Value("${homo.relational.mysql.connect.timeout.second:3}")
    private Integer timeoutSecond;

    /**
     * 数据库表名前缀（默认值 "homo_storage"）
     * 可用于多租户架构下的数据库表命名规范。
     */
    @Value("${homo.relational.mysql.database.prefix:homo_storage}")
    private String prefix;

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
    public void init() {
        if (!url.isEmpty()){
            // jdbc:mysql://mysql:30006/tpf_storage?Unicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
            int pathEndIdx = url.indexOf('?');
            String body = url.substring(0, pathEndIdx);
            int protoEndIdx = body.indexOf("://");
            // jdbc:mysql
            String protocol = body.substring(0, protoEndIdx);
            driver = protocol.split(":")[1];
            String linkUrl = body.substring(protoEndIdx + 3);
            int databaseStartIdx = linkUrl.indexOf('/');
            database = linkUrl.substring(databaseStartIdx + 1);
            String[] hostPort = linkUrl.substring(0, databaseStartIdx).split(":");
            host = hostPort[0];
            if (hostPort.length == 2) {
                port = Integer.parseInt(hostPort[1]);
            }
        }
        if (database.isEmpty()) {
            String idc = Foundation.server().getDataCenter();
            idc = idc.replace("-", "_");
            database = prefix + "_" + idc;
        }
        log.info("RelationalMysqlProperties init {}",this);
    }



}
