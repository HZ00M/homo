package com.homo.core.configurable.mysql;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
public class MysqlNamespaceProperties {
    @Value("${mysql.public.namespace:homo_mysql_config}")
    private String publicNamespace;
    @Value("${mysql.private.namespace:mysql-connect-info}")
    private String privateNamespace;
}
