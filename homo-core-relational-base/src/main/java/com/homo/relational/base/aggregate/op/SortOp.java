package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import com.homo.core.facade.relational.query.HomoSort;
import lombok.Data;

@Data
public class SortOp implements AggregationOp {
    private final HomoSort sort;

    public SortOp(HomoSort sort) {
        this.sort = sort;
    }

    @Override
    public OpType getOpType() {
        return OpType.SORT;
    }
}
