package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 投影
 * 选择需要的列，过滤无关数据
 */
@Data
public class ProjectOp implements AggregationOp {
    private static final List<Projection> NONE = Collections.emptyList();
    @Getter
    private final List<Projection> projections;
    public ProjectOp(Class<?> type) {
        this(NONE, getFromType(type));
    }
    public ProjectOp(String... fields) {
        this(Arrays.asList(fields));
    }
    public ProjectOp(List<String> fields) {
        this(NONE, fields.stream().map(Projection::new).collect(Collectors.toList()));
    }
    private ProjectOp(List<? extends Projection> current, List<? extends Projection> projections){
        Assert.notNull(current, "Current projections must not be null!");
        Assert.notNull(projections, "Projections must not be null!");
        this.projections = new ArrayList<>(current.size()+projections.size());
        this.projections.addAll(current);
        this.projections.addAll(projections);
    }

    private static List<Projection> getFromType(Class<?> type) {
        return Arrays.stream(BeanUtils.getPropertyDescriptors(type))
                .filter(it->{
                    Method readMethod = it.getReadMethod();
                    if (readMethod == null){
                        return false;
                    }
                    // exclude obj and default methods
                    if (ReflectionUtils.isObjectMethod(readMethod)){
                        return false;
                    }
                    return !readMethod.isDefault();
                })
                .map(PropertyDescriptor::getName)
                .map(Projection::new)
                .collect(Collectors.toList());
    }

    private ProjectOp and(Projection projection) {
        this.projections.add(projection);
        return this;
    }

    public ProjectOp andInclude(String name) {
        this.projections.add(new Projection(name));
        return this;
    }

    public ProjectOp andAlias(String name, String alias) {
        this.projections.add(new Projection(name, alias));
        return this;
    }

    public ProjectOp and(Class<?> type) {
        this.projections.addAll(getFromType(type));
        return this;
    }

    public ProjectOp previous(String name) {
        this.projections.add(new Projection(name, true));
        return this;
    }

    @Override
    public OpType getOpType() {
        return OpType.PROJECT;
    }

    @Data
    public static class Projection {
        private String name;
        private String alias;
        private boolean previous;
        public Projection(String name) {
            this.name = name;
        }
        public Projection(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }
        public Projection(String name, boolean previous) {
            this.name = name;
            this.previous = previous;
        }
    }
}
