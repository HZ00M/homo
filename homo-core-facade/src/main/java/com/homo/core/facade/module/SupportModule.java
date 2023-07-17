package com.homo.core.facade.module;

public interface SupportModule extends Module {

    default Integer getOrder() {
        return Integer.MIN_VALUE + 1;
    }
}
