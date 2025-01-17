package com.homo.relational.driver.mysql.utils;

import com.homo.core.facade.relational.schema.TableSchema;
import org.springframework.data.relational.core.sql.GroupBySpec;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.StatementMapper;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.query.BoundAssignments;
import org.springframework.data.r2dbc.query.BoundCondition;
import org.springframework.data.r2dbc.query.UpdateMapper;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.sql.*;
import org.springframework.data.relational.core.sql.operation.GroupByPreparedOperation;
import org.springframework.data.relational.core.sql.operation.InsertIgnorePreparedOperation;
import org.springframework.data.relational.core.sql.operation.ReplaceInfoPrepareOperation;
import org.springframework.data.relational.core.sql.operation.SelectPreparedOperation;
import org.springframework.data.relational.core.sql.render.RenderContext;
import org.springframework.lang.Nullable;
import org.springframework.r2dbc.core.PreparedOperation;
import org.springframework.r2dbc.core.binding.BindMarkers;
import org.springframework.r2dbc.core.binding.Bindings;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class MapperUtil {
    private UpdateMapper updateMapper;
    private R2dbcDialect dialect;
    private R2dbcConverter converter;


    public void initialize(R2dbcDialect dialect, R2dbcConverter converter) {
        updateMapper = new UpdateMapper(dialect, converter);
        MapperUtil.dialect = dialect;
        MapperUtil.converter = converter;
        log.info("MapperUtil initialize done!");
    }

    public PreparedOperation<Insert> createInsertPreparedOperation(StatementMapper.InsertSpec insertSpec, RenderContext renderContext, TableSchema tableSchema, Object[] args) {
        RelationalPersistentEntity<?> entity = getEntity(tableSchema);
        BindMarkers bindMarkers = dialect.getBindMarkersFactory().create();
        SqlIdentifier sqlIdentifier = TableNameUtil.getTableName(tableSchema.getDomainClass(), args);
        String sql = toSql(sqlIdentifier);
        Table table = Table.create(sql);
        BoundAssignments boundAssignments = updateMapper.getMappedObject(bindMarkers, insertSpec.getAssignments(), table, entity);
        Bindings bindings = boundAssignments.getBindings();
        InsertBuilder.InsertIntoColumnsAndValues insertBuilder = StatementBuilder.insert(table);
        InsertBuilder.InsertValuesWithBuild withBuild = (InsertBuilder.InsertValuesWithBuild) insertBuilder;
        for (Assignment assignment : boundAssignments.getAssignments()) {
            if (assignment instanceof AssignValue) {
                AssignValue assignValue = (AssignValue) assignment;
                insertBuilder.column(assignValue.getColumn());
                insertBuilder.value(assignValue.getValue());
            }
        }
        return new ReplaceInfoPrepareOperation<>(withBuild.build(), renderContext, bindings);
    }

    public String toSql(SqlIdentifier identifier) {
        Assert.notNull(identifier, "SqlIdentifier must not be null");
        return identifier.toSql(dialect.getIdentifierProcessing());
    }

    public RelationalPersistentEntity<?> getEntity(TableSchema tableSchema) {
        return converter.getMappingContext().getPersistentEntity(tableSchema.getDomainClass());
    }

    public RelationalPersistentEntity<?> getEntity(Table table) {
        return converter.getMappingContext().getPersistentEntity(table.getClass());
    }

    public static PreparedOperation<Insert> createInsertIgnorePreparedOperation(StatementMapper.InsertSpec insertSpec, RenderContext renderContext, TableSchema tableSchema, Object[] args) {
        RelationalPersistentEntity<?> entity = getEntity(tableSchema);
        BindMarkers bindMarkers = dialect.getBindMarkersFactory().create();
        Table table = Table.create(toSql(TableNameUtil.getTableName(tableSchema.getDomainClass(), args)));
        BoundAssignments boundAssignments = updateMapper.getMappedObject(bindMarkers, insertSpec.getAssignments(), table, entity);
        Bindings bindings = boundAssignments.getBindings();
        InsertBuilder.InsertIntoColumnsAndValues insertBuilder = StatementBuilder.insert(table);
        InsertBuilder.InsertValuesWithBuild withBuild = (InsertBuilder.InsertValuesWithBuild) insertBuilder;
        for (Assignment assignment : boundAssignments.getAssignments()) {
            if (assignment instanceof AssignValue) {
                AssignValue assignValue = (AssignValue) assignment;
                insertBuilder.column(assignValue.getColumn());
                insertBuilder.value(assignValue.getValue());
            }
        }
        return new InsertIgnorePreparedOperation<>(withBuild.build(), renderContext, bindings);
    }

    public PreparedOperation<Select> createSelectPreparedOperation(StatementMapper.SelectSpec selectSpec, RenderContext renderContext) {
        RelationalPersistentEntity<?> entity = getEntity(selectSpec.getTable());
        Table table = selectSpec.getTable();
        List<Expression> selectList = getSelectList(selectSpec, entity);
        SelectBuilder.SelectAndFrom selectAndFrom = StatementBuilder.select(selectList);
        if (selectSpec.isDistinct()) {
            selectAndFrom = selectAndFrom.distinct();
        }
        SelectBuilder.SelectFromAndJoin selectBuilder = selectAndFrom.from(table);
        BindMarkers bindMarkers = dialect.getBindMarkersFactory().create();
        Bindings bindings = Bindings.empty();
        CriteriaDefinition criteria = selectSpec.getCriteria();
        if (criteria != null && !criteria.isEmpty()) {
            BoundCondition mappedObject = updateMapper.getMappedObject(bindMarkers, criteria, table, entity);
            bindings = mappedObject.getBindings();
            Condition condition = mappedObject.getCondition();
            selectBuilder.where(condition);
        }
        if (selectSpec.getSort().isSorted()) {
            List<OrderByField> sort = new ArrayList<>();
            for (Sort.Order order : selectSpec.getSort()) {
                PersistentPropertyPath<? extends RelationalPersistentProperty> path = null;
                try {
                    PropertyPath propertyPath = forName(SqlIdentifier.quoted(order.getProperty()).getReference(), entity);
                    if (!isPathToJavaLangClassProperty(propertyPath)) {
                        path = converter.getMappingContext().getPersistentPropertyPath(propertyPath);
                    }
                } catch (MappingException | PropertyReferenceException e) {
                    log.warn("MappingException e {}", e.getMessage());
                }
                SqlIdentifier sqlIdentifier = path == null || path.getLeafProperty() == null ? SqlIdentifier.quoted(order.getProperty())
                        : path.getLeafProperty().getColumnName();
                OrderByField orderBy = OrderByField.from(table.column(sqlIdentifier))
                        .withNullHandling(order.getNullHandling());
                sort.add(order.isAscending() ? orderBy.asc() : orderBy.desc());
            }
            selectBuilder.orderBy(sort);
        }
        if (selectSpec.getLimit() > 0) {
            selectBuilder.limit(selectSpec.getLimit());
        }
        if (selectSpec.getOffset() > 0) {
            selectBuilder.offset(selectSpec.getOffset());
        }
        Select select = selectBuilder.build();
        return new SelectPreparedOperation<>(select, renderContext, bindings);
    }

    private static boolean isPathToJavaLangClassProperty(PropertyPath path) {
        return path.getType().equals(Class.class) && path.getLeafProperty().getOwningType().getType().equals(Class.class);
    }

    private static PropertyPath forName(String path, RelationalPersistentEntity<?> entity) {

        if (entity.getPersistentProperty(path) != null) {
            return PropertyPath.from(Pattern.quote(path), entity.getTypeInformation());
        }

        return PropertyPath.from(path, entity.getTypeInformation());
    }

    private static List<Expression> getSelectList(StatementMapper.SelectSpec selectSpec, @Nullable RelationalPersistentEntity<?> entity) {
        if (entity == null) {
            return selectSpec.getSelectList();
        }
        List<Expression> selectList = selectSpec.getSelectList();
        List<Expression> mapped = new ArrayList<>(selectList.size());
        for (Expression expression : selectList) {
            Expression mappedObject = updateMapper.getMappedObject(expression, entity);
            mapped.add(mappedObject);
        }
        return mapped;
    }

    public static PreparedOperation<?> createAggregateOperation(GroupBySpec groupBySpec, RenderContext renderContext, Object[] args) {
        TableSchema tableSchema = groupBySpec.getTable();
        RelationalPersistentEntity<?> entity = getEntity(tableSchema);
        Table table = Table.create(TableNameUtil.getTableName(tableSchema,args));
        GroupBySelectBuilder selectBuilder = new GroupBySelectBuilder()
                .select(groupBySpec.getSelectList())
                .from(table);
        BindMarkers bindMarkers = dialect.getBindMarkersFactory().create();
        Bindings bindings = Bindings.empty();
        CriteriaDefinition criteria = groupBySpec.getCriteria();
        if (criteria != null && !criteria.isEmpty()){
            BoundCondition mappedObject = updateMapper.getMappedObject(bindMarkers, criteria, table, entity);
            bindings = mappedObject.getBindings();
            selectBuilder.where(mappedObject.getCondition());
        }
        if (!groupBySpec.getGroupBy().isEmpty()){
            for (Expression expression : groupBySpec.getGroupBy()) {
                selectBuilder.groupBy(expression);
            }
        }
        if (groupBySpec.getSort().isSorted()){
            List<OrderByField> mappedOrder = new ArrayList<>();
            for (Sort.Order order : groupBySpec.getSort()) {
                if (tableSchema.hasColumn(order.getProperty())){
                    OrderByField orderByField = OrderByField.from(Column.create(order.getProperty(), table))
                            .withNullHandling(order.getNullHandling());
                    mappedOrder.add(orderByField);
                }else {
                    OrderByField orderByField = OrderByField.from(Column.aliased(order.getProperty(), table, order.getProperty()))
                            .withNullHandling(order.getNullHandling());
                    mappedOrder.add(order.isAscending()?orderByField.asc():orderByField.desc());
                }
            }
            selectBuilder.orderBy(mappedOrder);
        }
        if (groupBySpec.getLimit() > 0){
            selectBuilder.limit(groupBySpec.getLimit());
        }
        if (groupBySpec.getOffset() > 0){
            selectBuilder.offset(groupBySpec.getOffset());
        }
        if (!groupBySpec.getJoins().isEmpty()){
            groupBySpec.getJoins().forEach(selectBuilder::join);
        }
        GroupBySelect select = selectBuilder.build();
        return new GroupByPreparedOperation<>(select, renderContext, bindings);
    }
}
