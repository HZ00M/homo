package org.springframework.data.relational.core.sql.operation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.relational.core.sql.Insert;
import org.springframework.data.relational.core.sql.render.InsertIgnoreStatementVisitor;
import org.springframework.data.relational.core.sql.render.RenderContext;
import org.springframework.r2dbc.core.PreparedOperation;
import org.springframework.r2dbc.core.binding.BindTarget;
import org.springframework.r2dbc.core.binding.Bindings;

@RequiredArgsConstructor
public class InsertIgnorePreparedOperation<T> implements PreparedOperation<T> {
    private final T source;
    private final RenderContext renderContext;
    private final Bindings bindings;

    @Override
    public T getSource() {
        return source;
    }

    @Override
    public void bindTo(BindTarget target) {
        this.bindings.apply(target);
    }

    @Override
    public String toQuery() {
        if (this.source instanceof Insert){
            InsertIgnoreStatementVisitor visitor = new InsertIgnoreStatementVisitor(this.renderContext);
            ((Insert)this.source).visit(visitor);
            return visitor.getRenderedPart().toString();
        }
        throw new IllegalStateException("Cannot render " + this.getSource());
    }
}
