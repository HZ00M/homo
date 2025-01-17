package org.springframework.data.relational.core.sql.render;

import org.springframework.data.relational.core.sql.*;

public class SelectStatementVisitor extends DelegatingVisitor implements PartRenderer {
    private final RenderContext context;
    private final StringBuilder builder = new StringBuilder();

    private final SelectRenderContext selectRenderContext;

    private final StringBuilder selectList = new StringBuilder();
    private final StringBuilder from = new StringBuilder();
    private final StringBuilder join = new StringBuilder();
    private final StringBuilder where = new StringBuilder();

    private final SelectListVisitor selectListVisitor;
    private final FromClauseVisitor fromClauseVisitor;
    private final JoinVisitor joinVisitor;
    private final WhereClauseVisitor whereClauseVisitor;
    private final OrderByClauseVisitor orderByClauseVisitor;

    public SelectStatementVisitor(RenderContext context) {
        this.context = context;
        this.selectRenderContext = context.getSelectRenderContext();
        this.selectListVisitor = new SelectListVisitor(context, it -> {
            selectList.append(it);
        });
        this.fromClauseVisitor = new FromClauseVisitor(context, it -> {

            if (from.length() != 0) {
                from.append(", ");
            }

            from.append(it);
        });
        this.joinVisitor = new JoinVisitor(context, it -> {

            if (join.length() != 0) {
                join.append(", ");
            }

            join.append(it);
        });
        this.whereClauseVisitor = new WhereClauseVisitor(context, it -> {
            where.append(it);
        });
        this.orderByClauseVisitor = new OrderByClauseVisitor(context);
    }

    @Override
    public Delegation doEnter(Visitable segment) {
        if (segment instanceof SelectList) {
            return Delegation.delegateTo(selectListVisitor);
        }
        if (segment instanceof From) {
            return Delegation.delegateTo(fromClauseVisitor);
        }
        if (segment instanceof Join) {
            return Delegation.delegateTo(joinVisitor);
        }
        if (segment instanceof Where) {
            return Delegation.delegateTo(whereClauseVisitor);
        }
        if (segment instanceof OrderByField) {
            return Delegation.delegateTo(orderByClauseVisitor);
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
                builder.append(" ").append(join);
            }
            if (where.length() != 0) {
                appendWhereSegment(from, where, builder);
            }
            CharSequence orderBy = orderByClauseVisitor.getRenderedPart();
            boolean hasOrderBy = orderBy.length() != 0;
            if (hasOrderBy) {
                builder.append(" ORDER BY ").append(orderBy);
            }
            builder.append(selectRenderContext.afterOrderBy(hasOrderBy).apply(select));
            return Delegation.leave();
        }
        return Delegation.retain();
    }

    @Override
    public CharSequence getRenderedPart() {
        return builder;
    }

    public static void appendWhereSegment(StringBuilder from, StringBuilder where, StringBuilder builder) {

        if (where.toString().contains("json_extract")) {
            String[] split = where.toString().split("AND");
            for (int i = 0; i < split.length; i++) {
                if (split[i].contains("json_extract")) {
                    split[i] = split[i].replace(from + ".", "");
                    split[i] = split[i].replace("`", "");
                }
            }
            String replacedSql = String.join("AND", split);
            builder.append(" WHERE ").append(replacedSql);
        } else {
            builder.append(" WHERE ").append(where);
        }

    }
}
