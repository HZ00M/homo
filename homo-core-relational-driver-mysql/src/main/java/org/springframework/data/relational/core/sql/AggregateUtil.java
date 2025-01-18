package org.springframework.data.relational.core.sql;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import com.homo.core.facade.relational.schema.TableSchema;
import com.homo.relational.base.aggregate.HomoAggregation;
import com.homo.relational.base.aggregate.op.*;
import com.homo.relational.base.criteria.HomoCriteria;
import com.homo.relational.driver.mysql.utils.QueryConvertUtil;
import com.homo.relational.driver.mysql.utils.TableNameUtil;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class AggregateUtil {
    public static GroupBySpec convertSpec(HomoAggregation aggregation) {
        GroupBySpec spec = GroupBySpec.create(aggregation.getInputSchema());
        AggregationOp previousOp = null;
        for (AggregationOp op : aggregation.getPipeline()) {
            AggregationOp.OpType opType = op.getOpType();
            switch (opType){
                case GROUP:
                    spec = convertGroup((GroupOp) op, spec);
                    break;
                case SKIP:
                    spec = convertSkip((SkipOp) op, spec);
                    break;
                case COUNT:
                    spec = convertCount((CountOp) op, spec, previousOp);
                    break;
                case LIMIT:
                    spec = converLimit((LimitOp) op,spec);
                    break;
                case SORT:
                    spec = convertSort((SortOp) op, spec);
                    break;
                case MATCH:
                    spec = convertMatch((MatchOp) op, spec);
                    break;
                case PROJECT:
                case UNWIND:
                    break;
                case LOOKUP:
                    spec = convertLookUp((LookUpOp)op,spec);
                    break;
                case SUM:
                    spec = convertSum((SumOp) op, spec, previousOp);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported aggregation operation: " + opType);
            }
            previousOp = op;
        }
        if (spec.getSelectList().isEmpty()){
            spec = fillProjection(spec, aggregation);
        }
        return spec;
    }

    private static GroupBySpec fillProjection(GroupBySpec spec, HomoAggregation aggregation) {
        Optional<AggregationOp> projectOpOptional = aggregation.getPipeline().stream()
                .filter(op -> op.getOpType().equals(AggregationOp.OpType.PROJECT)).findFirst();
        List<LookUpOp> lookupOps = aggregation.getPipeline().stream()
                .filter(op -> op.getOpType().equals(AggregationOp.OpType.LOOKUP))
                .map(op->(LookUpOp)op).collect(Collectors.toList());
        if (projectOpOptional.isPresent()){
            ProjectOp projectOp = (ProjectOp) projectOpOptional.get();
            for (ProjectOp.Projection projection : projectOp.getProjections()) {
                Optional<LookUpOp> lookupOperation = lookupOps.stream().filter(op -> Arrays.stream(op.getJoinColumns()).anyMatch(col->projection.getName().startsWith(col))).findFirst();
                TableSchema tableSchema = spec.getTable();
                String tableName = tableSchema.getIdentifier().getText();
                String columnName = projection.getName();
                //判定映射字段是来自源表还是连接表
                if (lookupOperation.isPresent()){
                    tableName = lookupOperation.get().getFrom().getIdentifier().getText();
                    columnName = lookupOperation.get().getFrom().getColumnName(columnName);
                }else {
                    columnName = tableSchema.getColumnName(columnName);
                }
                Column column = Column.create(columnName,Table.create(tableName));
                if (StringUtils.hasText(projection.getAlias())){
                    column = column.as(projection.getAlias());
                }
                spec.select(column);
            }
        }else {
            spec = convertTypeProjection(spec, spec.getTable());
        }
        return spec;
    }

    private static GroupBySpec convertTypeProjection(GroupBySpec spec, TableSchema tableSchema) {
        ProjectOp projectOp = new ProjectOp(tableSchema.getDomainClass());
        Table table = Table.create(tableSchema.getIdentifier().getText());
        for (ProjectOp.Projection projection : projectOp.getProjections()) {
            Column column = Column.create(tableSchema.getColumnName(projection.getName()), table);
            if (StringUtils.hasText(projection.getAlias())) {
                column = column.as(projection.getAlias());
            }
            spec = spec.select(column);
        }
        return spec;
    }

    private static GroupBySpec convertSum(SumOp op, GroupBySpec spec, AggregationOp previousOp) {
        List<Expression> expressions = new ArrayList<>();
        Literal<CharSequence> literal = new Literal<>(op.getColumn());
        expressions.add(literal);
        SimpleFunction sum = SimpleFunction.create("sum",expressions).as(op.getAlias());
        return spec.select(sum);
    }

    private static GroupBySpec convertLookUp(LookUpOp op, GroupBySpec spec) {
        String localColumn = spec.getTable().getColumnName(op.getLocalField());
        Table leftTable = Table.create(spec.getTable().getIdentifier().getText());
        Column left = Column.create(localColumn, leftTable);
        String foreignColumn = op.getFrom().getColumnName(op.getForeignField());
        Table rightTable = Table.create(op.getFrom().getIdentifier().getText());
        Column right = Column.create(foreignColumn, rightTable);
        Comparison comparison = Comparison.create(left,"=",right);
        spec = spec.join(new Join(Join.JoinType.JOIN,rightTable,comparison));
        return spec;
    }

    private static GroupBySpec convertMatch(MatchOp op, GroupBySpec spec) {
        Criteria criteria = QueryConvertUtil.convertToCriteria((HomoCriteria) op.getCriteriaDefinition());
        spec = spec.withCriteria(criteria);
        return spec;
    }

    private static GroupBySpec convertSort(SortOp op, GroupBySpec spec) {
        Sort sort = QueryConvertUtil.convertToSort(op.getSort());
        spec = spec.withSort(sort);
        return spec;
    }

    private static GroupBySpec converLimit(LimitOp op, GroupBySpec spec) {
        spec = spec.limit(op.getMaxElements());
        return spec;
    }

    private static GroupBySpec convertCount(CountOp op, GroupBySpec spec,AggregationOp previous) {
        List<Expression> expressions = new ArrayList<>();
        if (previous == null || previous.getOpType() != AggregationOp.OpType.GROUP){
            Literal<CharSequence> literal = new Literal<>("*");
            expressions.add(literal);
        }else {
            GroupOp groupOp = (GroupOp) previous;
            Literal literal = new Literal("DISTINCT " + spec.getTable().getColumnName(groupOp.getField()));
            expressions.add(literal);
        }
        SimpleFunction sum = SimpleFunction.create("count",expressions).as(op.getColumn());
        return spec.select(sum);
    }

    private static GroupBySpec convertSkip(SkipOp op, GroupBySpec spec) {
        spec = spec.offset(op.getSkipCount());
        return spec;
    }

    private static GroupBySpec convertGroup(GroupOp op, GroupBySpec spec) {
        if (op.getOperations().isEmpty()){
            return spec;
        }
        TableSchema tableSchema = spec.getTable();
        String groupByColumnName = tableSchema.getColumnName(op.getField());
        SqlIdentifier tableName = TableNameUtil.getTableName(tableSchema, null);
        Table table = Table.create(tableName);
        Column column = Column.create(groupByColumnName, table);
        spec.groupBy(column);
        spec.select(column);
        for (GroupOp.GroupTargetOperation targetOperation : op.getOperations()) {
            AggregationOp.OpType opType = targetOperation.getOpType();
            switch (opType) {
                case SUM:
                    SimpleFunction sum = funcWithfColumn(targetOperation, "sum", spec.getTable());
                    spec.select(sum);
                    break;
                case COUNT:
                    StringLiteral literal = new StringLiteral("*");
                    SimpleFunction count = SimpleFunction.create("count", Collections.singletonList(literal)).as(targetOperation.getAlias());
                    spec.select(column);
                case AVG:
                    SimpleFunction avg = funcWithfColumn(targetOperation, "avg", spec.getTable());
                    spec.select(avg);
                    break;
                case MAX:
                    SimpleFunction max = funcWithfColumn(targetOperation, "max", spec.getTable());
                    spec.select(max);
                    break;
                case MIN:
                    SimpleFunction min = funcWithfColumn(targetOperation, "min", spec.getTable());
                    spec.select(min);
                    break;
                default:
                    throw new RuntimeException("Unknown group op!");
            }
        }
        return spec;
    }

    private static SimpleFunction funcWithfColumn(GroupOp.GroupTargetOperation operation, String func, TableSchema tableSchema) {
        String columnName = tableSchema.getColumnName(operation.getReference());
        Table table = Table.create(TableNameUtil.getTableName(tableSchema,null));
        Column column = Column.create(columnName,table);
        return SimpleFunction.create(func, Collections.singletonList(column)).as(operation.getAlias());
    }


}
