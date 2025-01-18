package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import lombok.Data;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class GroupOp implements AggregationOp {
    private final String field;
    private final List<GroupTargetOperation> operations;

    private GroupOp(String field) {
        this.field = field;
        this.operations = new ArrayList<>();
    }

    private GroupOp(GroupOp groupOperation, List<GroupTargetOperation> nextOperations) {

        Assert.notNull(groupOperation, "GroupOperation must not be null!");
        Assert.notNull(nextOperations, "NextOperations must not be null!");

        this.field = groupOperation.field;
        this.operations = new ArrayList<>(nextOperations.size() + 1);
        this.operations.addAll(groupOperation.operations);
        this.operations.addAll(nextOperations);
    }

    public static GroupOp create(String field){
        return new GroupOp(field);
    }

    protected GroupOp and(GroupTargetOperation operation) {
        return new GroupOp(this, Collections.singletonList(operation));
    }

    /**
     * 记录数
     * @return 属性名称
     */
    public Builder count(){
        return addGroupTargetOp(OpType.SUM,null,1);
    }

    /**
     * 记录数据求和
     * @param reference 需要求和的属性名
     * @return
     */
    public Builder sum(String reference){
        return addGroupTargetOp(OpType.SUM,reference,null);
    }

    /**
     * 平均值
     * @param reference 属性名称
     * @return
     */
    public Builder avg(String reference){
        return addGroupTargetOp(OpType.AVG,reference,null);
    }

    /**
     * 最大值
     * @param reference 属性名称
     * @return
     */
    public Builder max(String reference){
        return addGroupTargetOp(OpType.MAX,reference,null);
    }

    /**
     * 最小值
     * @param reference 属性名称
     * @return
     */
    public Builder min(String reference){
        return addGroupTargetOp(OpType.MIN,reference,null);
    }

    @Override
    public OpType getOpType() {
        return OpType.GROUP;
    }

    public static final class Builder {
        private final GroupOp groupOp;
        private final GroupTargetOperation operation;

        private Builder(GroupOp groupOp, GroupTargetOperation operation) {

            Assert.notNull(groupOp, "groupOp must not be null!");
            Assert.notNull(operation, "Operation must not be null!");

            this.groupOp = groupOp;
            this.operation = operation;
        }

        public GroupOp as(String alias) {
            return this.groupOp.and(operation.withAlias(alias));
        }
    }
    private Builder addGroupTargetOp(OpType opType, @Nullable String reference, @Nullable Object value) {
        return new Builder(this, new GroupTargetOperation(opType, null, reference, value));
    }

    @Data
    public static class GroupTargetOperation implements AggregationOp {
        private final OpType opType;
        private final @Nullable String alias;
        private final @Nullable String reference;
        private final @Nullable Object value;

        public GroupTargetOperation(OpType opType, @Nullable String alias, @Nullable String reference, @Nullable Object value) {
            this.opType = opType;
            this.alias = alias;
            this.reference = reference;
            this.value = value;
        }

        public GroupTargetOperation withAlias(String alias) {
            return new GroupTargetOperation(opType, alias, reference, value);
        }

        @Override
        public OpType getOpType() {
            return opType;
        }
    }
}
