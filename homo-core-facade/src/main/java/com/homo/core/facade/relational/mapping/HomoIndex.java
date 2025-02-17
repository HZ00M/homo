package com.homo.core.facade.relational.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HomoIndex {
    /**
     * 索引名，为空时根据字段名拼接
     * @return
     */
    String name() default "";

    /**
     * 索引相关的项
     * @return
     */
    String[] columns();

    IndexType indexType() default IndexType.NORMAL;

    enum IndexType {
        UNIQUE,
        NORMAL,
    }

}
