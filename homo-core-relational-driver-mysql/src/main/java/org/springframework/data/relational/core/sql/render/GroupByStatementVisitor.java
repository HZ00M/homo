package org.springframework.data.relational.core.sql.render;

import com.homo.relational.driver.mysql.utils.TpfJsonVisitorUtil;
import org.springframework.data.relational.core.sql.*;

public class GroupByStatementVisitor extends DelegatingVisitor implements PartRenderer {
    private final RenderContext context;
    private final SelectRenderContext selectRenderContext;

    private final StringBuilder builder = new StringBuilder();
    private final StringBuilder selectList = new StringBuilder();
    private final StringBuilder from = new StringBuilder();
    private final StringBuilder join = new StringBuilder();
    private final StringBuilder where = new StringBuilder();
    private final StringBuilder groupBy = new StringBuilder();

    private final SelectListVisitor selectListVisitor;
    private final OrderByClauseVisitor orderByClauseVisitor;
    private final FromClauseVisitor fromClauseVisitor;
    private final WhereClauseVisitor whereClauseVisitor;
    private final GroupByVisitor groupByVisitor;

    public GroupByStatementVisitor(RenderContext context) {

        this.context = context;
        this.selectRenderContext = context.getSelect();
        this.selectListVisitor = new SelectListVisitor(context, selectList::append);
        this.orderByClauseVisitor = new OrderByClauseVisitor(context);
        this.groupByVisitor = new GroupByVisitor(context, groupBy::append);
        this.fromClauseVisitor = new FromClauseVisitor(context, it -> {

            if (from.length() != 0) {
                from.append(", ");
            }

            from.append(it);
        });

        this.whereClauseVisitor = new WhereClauseVisitor(context, where::append);
    }

    @Override
    public Delegation doEnter(Visitable segment) {

        if (segment instanceof SelectList) {
            return Delegation.delegateTo(selectListVisitor);
        }

        if (segment instanceof OrderByField) {
            return Delegation.delegateTo(orderByClauseVisitor);
        }

        if (segment instanceof From) {
            return Delegation.delegateTo(fromClauseVisitor);
        }

        if (segment instanceof GroupBy) {
            return Delegation.delegateTo(groupByVisitor);
        }

        if (segment instanceof Join) {
            return Delegation.delegateTo(new JoinVisitor(context, it -> {

                if (join.length() != 0) {
                    join.append(' ');
                }

                join.append(it);
            }));
        }

        if (segment instanceof Where) {
            return Delegation.delegateTo(whereClauseVisitor);
        }

        return Delegation.retain();
    }

    @Override
    public Delegation doLeave(Visitable segment) {

        if (segment instanceof Select) {

            Select select = (Select) segment;

            builder.append("SELECT ");

            if (select.isDistinct()) {
                builder.append("DISTINCT ");
            }

            builder.append(selectList);
            builder.append(selectRenderContext.afterSelectList().apply(select));

            if (from.length() != 0) {
                builder.append(" FROM ").append(from);
            }

            builder.append(selectRenderContext.afterFromTable().apply(select));

            if (join.length() != 0) {
                builder.append(' ').append(join);
            }

            if (where.length() != 0) {
                TpfJsonVisitorUtil.visit(from, where, builder);
            }

            if (groupBy.length() != 0) {
                builder.append(" GROUP BY ").append(groupBy);
            }

            CharSequence orderBy = orderByClauseVisitor.getRenderedPart();
            if (orderBy.length() != 0) {
                builder.append(" ORDER BY ").append(orderBy);
            }

            builder.append(selectRenderContext.afterOrderBy(orderBy.length() != 0).apply(select));

            return Delegation.leave();
        }

        return Delegation.retain();
    }

    @Override
    public CharSequence getRenderedPart() {
        return builder;
    }
}
