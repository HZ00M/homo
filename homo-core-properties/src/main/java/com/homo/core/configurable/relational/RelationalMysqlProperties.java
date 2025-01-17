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
public class RelationalMysqlProperties implements InitializingBean {
    @Value("${homo.relational.mysql.connect.port:}")
    private int port;

    @Value("${homo.relational.mysql.connect.host:}")
    private String host;

    @Value("${homo.relational.mysql.connect.database:}")
    private String database;

    @Value("${homo.relational.mysql.connect.driver:mysql}")
    private String driver;
    // jdbc:mysql://mysql:30006/tpf_storage?Unicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
    // r2dbc:mysql://mysql:30006/tpf_storage?useSSL=false&characterEncoding=UTF-8
    @Value("${homo.relational.mysql.connect.url:}")
    private String url;

    @Value("${homo.relational.mysql.connect.username:}")
    private String username;

    @Value("${homo.relational.mysql.connect.password:}")
    private String password;

    @Value("${homo.relational.mysql.connect.timeout.second:3}")
    private Integer timeoutSecond;

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
