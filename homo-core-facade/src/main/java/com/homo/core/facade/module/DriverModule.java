package com.homo.core.facade.module;

public interface DriverModule extends Module {

    default Integer getOrder() {
        return Integer.MIN_VALUE + 2;
    }
}
