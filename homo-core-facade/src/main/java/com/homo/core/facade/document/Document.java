package com.homo.core.facade.document;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Document {
    /**
     * 文档名
     */
    String collectionName() ;

    /**
     * 索引列表
     * @return
     */
    String[] indexes() default {};

    IndexType indexType() default IndexType.ASC;

    enum IndexType{
        ASC,
        DESC
    }
}
