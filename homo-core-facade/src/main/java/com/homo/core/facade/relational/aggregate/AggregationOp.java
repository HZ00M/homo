package com.homo.core.facade.relational.aggregate;

public interface AggregationOp {
    OpType getOpType();

    enum OpType {
        PROJECT("project"),
        COUNT("count"),
        GROUP("group"),
        SKIP("skip"),
        LIMIT("limit"),
        SORT("sort"),
        MATCH("match"),
        UNWIND("unwind"),
        LOOKUP("lookup"),
        SUM("sum"),
        LAST("last"),
        FIRST("first"),
        AVG("avg"),
        MIN("min"),
        MAX("max"),
        ;

        private final String op;

        OpType(String op) {
            this.op = op;
        }
    }
}
