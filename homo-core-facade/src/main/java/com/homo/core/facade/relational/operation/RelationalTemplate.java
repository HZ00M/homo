package com.homo.core.facade.relational.operation;

/**
 * 所有关系型的操作接口
 */
public interface RelationalTemplate<P extends AggregateOperation.Aggregation> extends InsertOperation,SelectOperation,UpdateOperation,DeleteOperation,ExecuteOperation,AggregateOperation<P>{
    default String driverName(){
        return "";
    }
}
