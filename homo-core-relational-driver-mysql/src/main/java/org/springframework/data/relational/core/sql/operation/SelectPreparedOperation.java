package org.springframework.data.relational.core.sql.operation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.Visitable;
import org.springframework.data.relational.core.sql.render.RenderContext;
import org.springframework.data.relational.core.sql.render.SelectStatementVisitor;
import org.springframework.r2dbc.core.PreparedOperation;
import org.springframework.r2dbc.core.binding.BindTarget;
import org.springframework.r2dbc.core.binding.Bindings;

@Slf4j
public class SelectPreparedOperation<T extends Visitable> implements PreparedOperation<T > {
    private final T source;
    private final RenderContext renderContext;
    private final Bindings bindings;
    public SelectPreparedOperation(T source, RenderContext renderContext, Bindings bindings) {
        this.source = source;
        this.renderContext = renderContext;
        this.bindings = bindings;
    }
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
        if (this.source instanceof Select){
            SelectStatementVisitor visitor = new SelectStatementVisitor(this.renderContext);
            source.visit(visitor);
            return visitor.getRenderedPart().toString();
        }
        throw new IllegalStateException("Cannot render " + this.getSource());
    }
}
