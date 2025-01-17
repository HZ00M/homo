package com.homo.relational.base.aggregate;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import com.homo.core.facade.relational.operation.AggregateOperation;
import com.homo.core.facade.relational.query.HomoSort;
import com.homo.core.facade.relational.query.criteria.HomoCriteriaDefinition;
import com.homo.core.facade.relational.schema.TableSchema;
import com.homo.relational.base.SchemaInfoCoordinator;
import com.homo.relational.base.aggregate.op.*;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class HomoAggregation implements AggregateOperation.Aggregation {
    /**
     * 聚合使用的源表
     */
//    private final Class<?> inputType;

    private final TableSchema inputSchema;

    private final List<AggregationOp> pipeline;


    private HomoAggregation(TableSchema inputSchema, AggregationOp... operations) {
//        this.inputType = inputType;
        this.inputSchema = inputSchema;
        this.pipeline = new ArrayList<>();
        Collections.addAll(pipeline, operations);
    }


    /**
     * 根据聚合选项构建一个聚合操作对象
     * Params:
     * type – 聚合操作的结果类型 operations – 聚合操作
     * Returns:
     * 聚合操作条件对象
     */
    public static <T> HomoAggregation newAggregation(Class<T> inputType, AggregationOp... operations) {
        Assert.notEmpty(operations, "AggregationOperations must not be null or empty!");
        TableSchema tableSchema = SchemaInfoCoordinator.getTable(inputType);
        return new HomoAggregation(tableSchema, operations);
    }

    /**
     * 添加管线操作
     * Params:
     * operation – 聚合操作
     */
    public void addOperation(AggregationOp operation) {
        Assert.notNull(operation, "AggregationOperation must not be null!");
        pipeline.add(operation);
    }

    public static Builder newBuilder(Class<?> inputType) {
        return new Builder(inputType);
    }

    public static final class Builder {
        private Class<?> inputType;
        @Getter
        private List<AggregationOp> pipeline;

        public Builder(Class<?> inputType) {
            this.inputType = inputType;
            this.pipeline = new ArrayList<>();
        }

        public Builder addOperation(AggregationOp operation) {
            pipeline.add(operation);
            return this;
        }

        public HomoAggregation build() {
            return HomoAggregation.newAggregation(inputType, pipeline.toArray(new AggregationOp[0]));
        }

        /**
         * 根据属性聚合
         *
         * @param field 属性名称
         * @return 聚合选项
         */
        public Builder group(String field) {
            return addOperation(new GroupOp(field));
        }

        ;

        /**
         * 分页操作，跳过指定数量的数据
         *
         * @param elementsToSkip 要跳过的数据数量
         */
        public Builder skip(long elementsToSkip) {
            return addOperation(new SkipOp(elementsToSkip));
        }

        /**
         * 排序操作
         *
         * @param orders 排序项
         */
        public Builder sort(HomoSort.Order... orders) {
            return addOperation(new SortOp(HomoSort.by(orders)));
        }

        /**
         * 限制返回条数
         * Params:
         * limit – 数量
         */
        public Builder limit(int limit) {
            return addOperation(new LimitOp(limit));
        }

        /**
         * count id 聚合后的数据总量 [{id: 1, value: 2}, {id: 2, value: 2}] => COUNT() => 2
         * Returns:
         * 聚合后的数据总量
         */
        public Builder count(String column) {
            return addOperation(new CountOp(column));
        }

        /**
         * sum value 聚合后的数据总量 [{id: 1, value: 2}, {id: 2, value: 3}] => SUM() => 5
         * Returns:
         * sum计算后数值
         */
        public Builder sum(String column) {
            String alias = String.format("sum_%s", column);
            return addOperation(new SumOp(column, alias));
        }

        /**
         * 条件聚合 [{id: 1, value: 2}, {id: 2, value: 3}] => match(WHERE("id").is(1)) => [{id: 1, value: 2}
         * Params:
         * criteriaDefinition – 聚合条件
         */
        public Builder match(HomoCriteriaDefinition criteriaDefinition){
            return addOperation(new MatchOp(criteriaDefinition));
        }

        /**
         * 性映射 { id: 1, value: 2, city: "test"} => project(id, value) => { id: 1, value: 2}
         * Params:
         * fields – 需要取哪些字段到结果中
         */
        public Builder project(String... fields){
            return addOperation(new ProjectOp(fields));
        }

        /**
         * 映射类的所有属性 class Test { private int id; private int value; } => project(Test. class) => project(id, value)
         * Params:
         * domainType – 类对象
         */
        public Builder project(Class<?> domainType){
            return addOperation(new ProjectOp(domainType));
        }

        /**
         * 将一条记录拆分为多条 { id: 1, doc:[{value: 1}, {value: 2}]} => unwind(doc) => [{id: 1, value: 1}, {id: 1, value: 2}]
         * Params:
         * field – 属性名
         */
        public Builder unwind(String field){
            return addOperation(new UnwindOp(field,false));
        }

        /**
         * 联合查询 sql: select A.*, B.* from A join B on A. localField = B. foreignField
         * Params:
         * from – 需要join的表名 localField – A表条件字段 foreignField – B表条件字段 alias – 映射匹配记录的名称
         */
        public Builder lookup(String from, String localField, String foreignField, String alias){
            TableSchema tableSchema = SchemaInfoCoordinator.getTable(from);
            return addOperation(new LookUpOp(tableSchema, localField, foreignField, alias));
        }
    }
}
