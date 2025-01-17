package com.homo.core.facade.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class ServiceInfo {
    public String serviceTag;
    public String serverHost;
    public Integer serverPort;
    public boolean isStateful;
    public Integer driverType;
}
