package com.homo.core.configurable.gate;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
@ToString
public class GateCommonProperties {
    /**
     * 消息版本号
     */
    @Value("${homo.gate.message.version:1}")
    public  Integer version;

}
