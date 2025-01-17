package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import com.homo.core.facade.relational.schema.TableSchema;
import lombok.Data;

@Data
public class LookUpOp implements AggregationOp {
    private final TableSchema from;
    private final String localField;
    private final String foreignField;
    private final String alias;

    public LookUpOp(TableSchema from, String localField, String foreignField, String alias) {
        this.from = from;
        this.localField = localField;
        this.foreignField = foreignField;
        this.alias = alias;
    }
    @Override
    public OpType getOpType() {
        return OpType.LOOKUP;
    }
}
