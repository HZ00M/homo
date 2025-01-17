package org.springframework.data.relational.core.sql.render;

import org.springframework.data.relational.core.sql.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReplaceIntoStatementVisitor extends DelegatingVisitor implements PartRenderer {
    private final StringBuilder builder = new StringBuilder();
    private final StringBuilder into = new StringBuilder();
    private final List<String> columns = new ArrayList<>();
    private final StringBuilder values = new StringBuilder();

    private final IntoClauseVisitor intoClauseVisitor;
    private final ColumnVisitor columnVisitor;
    private final ValuesVisitor valuesVisitor;

    public ReplaceIntoStatementVisitor(RenderContext context) {
        this.intoClauseVisitor = new IntoClauseVisitor(context, it -> {
            if (into.length() != 0) {
                into.append(", ");
            }
            into.append(it);
        });
        this.columnVisitor = new ColumnVisitor(context, false, it -> {
            columns.add(it.toString());
        });
        this.valuesVisitor = new ValuesVisitor(context, values::append);
    }

    @Override
    public Delegation doEnter(Visitable segment) {
        if (segment instanceof Into) {
            return Delegation.delegateTo(this.intoClauseVisitor);
        }
        if (segment instanceof Column) {
            return Delegation.delegateTo(this.columnVisitor);
        }
        if (segment instanceof Values) {
            return Delegation.delegateTo(this.valuesVisitor);
        }
        return Delegation.retain();
    }

    @Override
    public Delegation doLeave(Visitable segment) {
        if (segment instanceof Insert) {
            builder.append("INSERT INTO ").append(into);
            if (!columns.isEmpty()){
                builder.append(" (").append(String.join(", ", columns)).append(") ");
            }
            builder.append("VALUES (").append(values).append(")");
            if (!columns.isEmpty()){
                builder.append(" ON DUPLICATE KEY UPDATE ");
                Iterator<String> it = columns.iterator();
                while (it.hasNext()) {
                    String column = it.next();
                    builder.append(column).append("=VALUES(").append(column).append(")");
                    if (it.hasNext()){
                        builder.append(",");
                    }
                }
            }
        }
        return Delegation.leave();
    }

    @Override
    public CharSequence getRenderedPart() {
        return builder;
    }
}
