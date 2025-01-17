package org.springframework.data.relational.core.sql.render;

import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.GroupBy;
import org.springframework.data.relational.core.sql.Visitable;

public class GroupByVisitor extends TypedSubtreeVisitor<GroupBy> implements PartRenderer{
    private final StringBuilder builder = new StringBuilder();
    private final RenderTarget target;
    private boolean requiresComma = false;
    private final ExpressionVisitor expressionVisitor;

    public GroupByVisitor(RenderContext context, RenderTarget target) {
        this.target = target;
        this.expressionVisitor = new ExpressionVisitor(context);
    }

    @Override
    Delegation enterNested(Visitable segment) {
        if (requiresComma) {
            builder.append(", ");
            requiresComma = false;
        }
        if (segment instanceof Expression) {
            return Delegation.delegateTo(expressionVisitor);
        }
        return super.enterNested(segment);
    }
    @Override
    Delegation leaveMatched(GroupBy segment) {
        target.onRendered(builder);
        return super.leaveMatched(segment);
    }

    @Override
    Delegation leaveNested(Visitable segment) {
        if (segment instanceof Expression) {
            builder.append(expressionVisitor.getRenderedPart());
            requiresComma = true;
        }
        return super.leaveNested(segment);
    }

    @Override
    public CharSequence getRenderedPart() {
        return builder;
    }
}
