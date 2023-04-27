package com.homo.core.common.module;

public interface SupportModule extends Module {

    default Integer getOrder() {
        return Integer.MIN_VALUE + 1;
    }
}
