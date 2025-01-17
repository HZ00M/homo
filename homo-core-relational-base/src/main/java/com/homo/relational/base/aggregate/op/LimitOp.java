package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import lombok.Data;
import org.springframework.util.Assert;

@Data
public class LimitOp implements AggregationOp {
    private final int maxElements;

    public LimitOp(int maxElements) {
        Assert.isTrue(maxElements >= 0, "Maximum number of elements must be greater or equal to zero!");
        this.maxElements = maxElements;
    }
    @Override
    public OpType getOpType() {
        return OpType.LIMIT;
    }
}
