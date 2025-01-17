package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import lombok.Data;
import org.springframework.util.Assert;

@Data
public class SumOp implements AggregationOp {
    private final String column;
    private final String alias;

    public SumOp(String column, String alias) {
        Assert.hasText(column, "column name must not be null or empty!");
        Assert.hasText(alias, "fieldName name must not be null or empty!");
        this.column = column;
        this.alias = alias;
    }
    @Override
    public OpType getOpType() {
        return OpType.SUM;
    }

    public static class Builder {
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
