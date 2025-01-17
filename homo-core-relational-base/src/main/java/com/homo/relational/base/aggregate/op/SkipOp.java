package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import lombok.Data;
import org.springframework.util.Assert;

@Data
public class SkipOp implements AggregationOp {
    private final long skipCount;

    public SkipOp(long skipCount) {
        Assert.isTrue(skipCount >= 0, "Skip count must not be negative!");
        this.skipCount = skipCount;
    }

    @Override
    public OpType getOpType() {
        return OpType.SKIP;
    }
}
