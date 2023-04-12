package com.homo.core.tread.tread.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class TreadProperties {

    @Value("${tpf.check.scanPath:com.syyx}")
    public String scanPath;

    @Value("${tpf.check.trace.enable:false}")
    public boolean traceEnable;
}
