package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import lombok.Data;
import org.springframework.util.Assert;

@Data
public class CountOp implements AggregationOp {
    private final String column;
    private final String alias;

    /**
     * 根据fieldName 分组
     * @param column 属性
     */
    public CountOp(String column, String alias) {
        Assert.hasText(column, "Field name must not be null or empty!");
        this.column = column;
        this.alias = alias;
    }

    @Override
    public OpType getOpType() {
        return OpType.COUNT;
    }

    public static class Builder{
        String column;
        public Builder(String column) {
            this.column = column;
        }

        /**
         * @param alias 别名
         */
        public SumOp as(String alias) {
            return new SumOp(column, alias);
        }
    }
}
