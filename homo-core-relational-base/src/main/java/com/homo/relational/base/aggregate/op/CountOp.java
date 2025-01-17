package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import lombok.Data;
import org.springframework.util.Assert;

@Data
public class CountOp implements AggregationOp {
    private final String fieldName;

    /**
     * 根据fieldName 分组
     * @param fieldName 属性
     */
    public CountOp(String fieldName) {
        Assert.hasText(fieldName, "Field name must not be null or empty!");
        this.fieldName = fieldName;
    }

    @Override
    public OpType getOpType() {
        return OpType.COUNT;
    }

    public static class Builder{
        /**
         * 分组后别名
         * Params:
         * fieldName – 别名
         * @param fieldName
         * @return
         */
        public CountOp as(String fieldName) {
            return new CountOp(fieldName);
        }
    }
}
