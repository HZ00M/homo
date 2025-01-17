package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;

public class UnwindOp implements AggregationOp {
    private final String field;
    private final boolean preserveNullAndEmptyArrays;

    public UnwindOp(String field, boolean preserveNullAndEmptyArrays) {
        this.field = field;
        this.preserveNullAndEmptyArrays = preserveNullAndEmptyArrays;
    }
    @Override
    public OpType getOpType() {
        return OpType.UNWIND;
    }
}
