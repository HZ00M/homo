package com.homo.core.facade.relational.mapping;

public class DefaultHomoTableDivideStrategy implements HomoTableDivideStrategy{

    public String genTableName(String tableName, Object[] args){
        return tableName; // default to use original table name
    }
}
