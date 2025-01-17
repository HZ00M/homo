package org.springframework.data.relational.core.sql;

import com.homo.core.facade.relational.schema.TableSchema;
import lombok.Data;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupBySpec {
    private TableSchema table;
    private List<String> projectedFields;
    private List<Expression> selectList;
    private List<Expression> groupBy;
    private CriteriaDefinition criteria;
    private Sort sort;
    private long offset;
    private int limit;
    private boolean distinct;
    private List<Join> joins;

    protected GroupBySpec(TableSchema table, List<String> projectedFields, List<Expression> selectList,List<Expression> groupBy, List<Join> joins, CriteriaDefinition criteria, Sort sort, int limit, long offset, boolean distinct) {
        this.table = table;
        this.projectedFields = projectedFields;
        this.selectList = selectList;
        this.criteria = criteria;
        this.sort = sort;
        this.offset = offset;
        this.limit = limit;
        this.distinct = distinct;
        this.groupBy = groupBy;
        this.joins = joins;
    }

    public static GroupBySpec create(TableSchema table) {
        List<String> projectedFields = new ArrayList<>();
        List<Expression> selectList = new ArrayList<>();
        List<Expression> groupByList = new ArrayList<>();
        List<Join> joins = new ArrayList<>();
        return new GroupBySpec(table, projectedFields, selectList, groupByList, joins, Criteria.empty(), Sort.unsorted(), -1, -1,
                false);
    }

    public GroupBySpec withCriteria(CriteriaDefinition criteria) {
        this.criteria = criteria;
        return this;
    }

    public GroupBySpec withSort(Sort sort) {
        this.sort = sort;
        return this;
    }

    public GroupBySpec offset(long offset) {
        this.offset = offset;
        return this;
    }

    public GroupBySpec limit(int limit) {
        this.limit = limit;
        return this;
    }

    public GroupBySpec groupBy(Expression expression) {
        this.groupBy.add(expression);
        return this;
    }

    public GroupBySpec select(Expression expression) {
        this.selectList.add(expression);
        return this;
    }

    public GroupBySpec join(Join join) {
        this.joins.add(join);
        return this;
    }
}
