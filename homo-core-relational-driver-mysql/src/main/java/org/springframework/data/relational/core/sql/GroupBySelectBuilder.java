package org.springframework.data.relational.core.sql;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GroupBySelectBuilder implements SelectBuilder, SelectBuilder.SelectAndFrom, SelectBuilder.SelectFromAndJoin, SelectBuilder.SelectWhereAndOr {
    private boolean distinct = false;
    private final List<Expression> selectList = new ArrayList<>();
    private final List<Expression> groupByList = new ArrayList<>();
    private final List<TableLike> from = new ArrayList<>();
    private long limit = -1;
    private long offset = -1;
    private final List<Join> joins = new ArrayList<>();
    private @Nullable
    Condition where;
    private final List<OrderByField> orderBy = new ArrayList<>();
    private @Nullable LockMode lockMode;

    @Override
    public SelectBuilder top(int count) {
        limit = count;
        return this;
    }

    @Override
    public GroupBySelectBuilder select(Expression expression) {
        selectList.add(expression);
        return this;
    }

    @Override
    public GroupBySelectBuilder select(Expression... expressions) {
        selectList.addAll(Arrays.asList(expressions));
        return this;
    }

    @Override
    public GroupBySelectBuilder select(Collection<? extends Expression> expressions) {
        selectList.addAll(expressions);
        return this;
    }

    @Override
    public GroupBySelectBuilder distinct() {
        distinct = true;
        return this;
    }

    @Override
    public SelectFromAndJoin from(String table) {
        return from(Table.create(table));
    }

    @Override
    public GroupBySelectBuilder from(TableLike table) {
        from.add(table);
        return this;
    }

    @Override
    public SelectFromAndJoin from(TableLike... tables) {
        from.addAll(Arrays.asList(tables));
        return this;
    }

    @Override
    public SelectFromAndJoin from(Collection<? extends TableLike> tables) {
        from.addAll(tables);
        return this;
    }

    @Override
    public SelectFromAndJoin limitOffset(long limit, long offset) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    @Override
    public SelectFromAndJoin limit(long limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public SelectFromAndJoin offset(long offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public GroupBySelectBuilder orderBy(OrderByField... orderByFields) {

        this.orderBy.addAll(Arrays.asList(orderByFields));

        return this;
    }

    @Override
    public GroupBySelectBuilder orderBy(Collection<? extends OrderByField> orderByFields) {

        this.orderBy.addAll(orderByFields);

        return this;
    }

    @Override
    public GroupBySelectBuilder orderBy(Column... columns) {

        for (Column column : columns) {
            this.orderBy.add(OrderByField.from(column));
        }

        return this;
    }

    @Override
    public SelectWhereAndOr where(Condition condition) {

        where = condition;
        return this;
    }

    @Override
    public SelectWhereAndOr and(Condition condition) {

        where = where.and(condition);
        return this;
    }

    @Override
    public SelectWhereAndOr or(Condition condition) {
        where = where.or(condition);
        return this;
    }

    @Override
    public SelectOn join(String table) {
        return join(Table.create(table));
    }

    @Override
    public SelectOn join(TableLike table) {
        return new JoinBuilder(table, this);
    }

    @Override
    public SelectOn leftOuterJoin(TableLike table) {
        return new JoinBuilder(table, this, Join.JoinType.LEFT_OUTER_JOIN);
    }

    public GroupBySelectBuilder join(Join join) {
        this.joins.add(join);

        return this;
    }

    @Override
    public SelectLock lock(LockMode lockMode) {

        this.lockMode = lockMode;
        return this;
    }

    public SelectBuilder groupBy(Expression group) {
        this.groupByList.add(group);
        return this;
    }

    @Override
    public GroupBySelect build() {
        GroupBySelect select = new GroupBySelect(selectList,groupByList, from, limit, offset, joins, where, orderBy, lockMode);
        SelectValidator.validate(select);
        return select;
    }

    static class JoinBuilder implements SelectOn, SelectOnConditionComparison, SelectFromAndJoinCondition {

        private final TableLike table;
        private final GroupBySelectBuilder selectBuilder;
        private final Join.JoinType joinType;
        private @Nullable Expression from;
        private @Nullable Expression to;
        private @Nullable Condition condition;

        JoinBuilder(TableLike table, GroupBySelectBuilder selectBuilder, Join.JoinType joinType) {

            this.table = table;
            this.selectBuilder = selectBuilder;
            this.joinType = joinType;
        }

        JoinBuilder(TableLike table, GroupBySelectBuilder selectBuilder) {
            this(table, selectBuilder, Join.JoinType.JOIN);
        }

        @Override
        public SelectOnConditionComparison on(Expression column) {

            this.from = column;
            return this;
        }

        @Override
        public SelectFromAndJoinCondition on(Condition condition) {
            if (this.condition == null) {
                this.condition = condition;
            } else {
                this.condition = this.condition.and(condition);
            }
            return this;
        }

        @Override
        public JoinBuilder equals(Expression column) {
            this.to = column;
            return this;
        }

        @Override
        public SelectOnConditionComparison and(Expression column) {

            finishCondition();
            this.from = column;
            return this;
        }

        private void finishCondition() {
            Comparison comparison = Comparison.create(from, "=", to);

            if (condition == null) {
                condition = comparison;
            } else {
                condition = condition.and(comparison);
            }

        }

        private Join finishJoin() {
            finishCondition();
            return new Join(joinType, table, condition);
        }

        @Override
        public SelectOrdered orderBy(OrderByField... orderByFields) {
            selectBuilder.join(finishJoin());
            return selectBuilder.orderBy(orderByFields);
        }

        @Override
        public SelectOrdered orderBy(Collection<? extends OrderByField> orderByFields) {
            selectBuilder.join(finishJoin());
            return selectBuilder.orderBy(orderByFields);
        }

        @Override
        public SelectOrdered orderBy(Column... columns) {
            selectBuilder.join(finishJoin());
            return selectBuilder.orderBy(columns);
        }

        @Override
        public SelectWhereAndOr where(Condition condition) {
            selectBuilder.join(finishJoin());
            return selectBuilder.where(condition);
        }

        @Override
        public SelectOn join(String table) {
            selectBuilder.join(finishJoin());
            return selectBuilder.join(table);
        }


        @Override
        public SelectOn join(TableLike table) {
            selectBuilder.join(finishJoin());
            return selectBuilder.join(table);
        }


        @Override
        public SelectOn leftOuterJoin(TableLike table) {
            selectBuilder.join(finishJoin());
            return selectBuilder.leftOuterJoin(table);
        }

        @Override
        public SelectFromAndJoin limitOffset(long limit, long offset) {
            selectBuilder.join(finishJoin());
            return selectBuilder.limitOffset(limit, offset);
        }

        @Override
        public SelectFromAndJoin limit(long limit) {
            selectBuilder.join(finishJoin());
            return selectBuilder.limit(limit);
        }

        @Override
        public SelectFromAndJoin offset(long offset) {
            selectBuilder.join(finishJoin());
            return selectBuilder.offset(offset);
        }

        @Override
        public SelectLock lock(LockMode lockMode) {
            selectBuilder.join(finishJoin());
            return selectBuilder.lock(lockMode);
        }

        @Override
        public Select build() {
            selectBuilder.join(finishJoin());
            return selectBuilder.build();
        }
    }
}
