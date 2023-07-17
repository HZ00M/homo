package com.homo.core.facade.module;

public interface ServiceModule extends Module {

    default Integer getOrder() {
        return Integer.MIN_VALUE + 3;
    }
}
