package com.homo.core.tread.tread.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TreadProperties {

    @Value("${tpf.check.scanPath:com.syyx}")
    public String scanPath;

    @Value("${tpf.check.trace.enable:false}")
    public boolean traceEnable;
}
