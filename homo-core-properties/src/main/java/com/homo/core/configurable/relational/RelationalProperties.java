package com.homo.core.configurable.relational;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class RelationalProperties {
    /**
     * 类扫描路径
     */
    @Value("${homo.relational.base.package:com.homo}")
    private String basePackage;
}
