package org.springframework.data.relational.core.sql;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;

public class GroupBySelect implements Select {
    private final SelectList selectList;
    private final GroupBy groupBy;
    private final From from;
    private final long limit;
    private final long offset;
    private final List<Join> joins;
    private final @Nullable Where where;
    private final List<OrderByField> orderBy;
    private final @Nullable LockMode lockMode;

    GroupBySelect(List<Expression> selectList,List<Expression> groupBy, List<TableLike> from, long limit, long offset,
                  List<Join> joins, @Nullable Condition where, List<OrderByField> orderBy, @Nullable LockMode lockMode) {

        this.selectList = new SelectList(new ArrayList<>(selectList));
        this.from = new From(new ArrayList<>(from));
        this.limit = limit;
        this.offset = offset;
        this.joins = new ArrayList<>(joins);
        this.orderBy = Collections.unmodifiableList(new ArrayList<>(orderBy));
        this.where = where != null ? new Where(where) : null;
        this.lockMode = lockMode;
        this.groupBy = new GroupBy(new ArrayList<>(groupBy));
    }

    @Override
    public From getFrom() {
        return this.from;
    }

    @Override
    public List<OrderByField> getOrderBy() {
        return this.orderBy;
    }

    @Override
    public OptionalLong getLimit() {
        return limit == -1 ? OptionalLong.empty() : OptionalLong.of(limit);
    }

    @Override
    public OptionalLong getOffset() {
        return offset == -1 ? OptionalLong.empty() : OptionalLong.of(offset);
    }

    @Override
    public boolean isDistinct() {
        return false;
    }

    @Nullable
    @Override
    public LockMode getLockMode() {
        return lockMode;
    }

    @Override
    public void visit(Visitor visitor) {

        Assert.notNull(visitor, "Visitor must not be null!");

        visitor.enter(this);

        selectList.visit(visitor);
        from.visit(visitor);
        joins.forEach(it -> it.visit(visitor));

        visitIfNotNull(where, visitor);

        orderBy.forEach(it -> it.visit(visitor));
        groupBy.visit(visitor);

        visitor.leave(this);
    }

    private void visitIfNotNull(@Nullable Visitable visitable, Visitor visitor) {

        if (visitable != null) {
            visitable.visit(visitor);
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
