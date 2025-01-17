package com.homo.core.facade.relational.query;

import com.homo.core.facade.relational.query.criteria.HomoCriteriaDefinition;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class HomoQuery<T extends HomoCriteriaDefinition<T>> {
    public static final int DEFAULT_OFFSET = -1;
    public static final int DEFAULT_LIMIT = -1;
    private final T criteria;
    private final List<String> columns;
    private final HomoSort sort;
    private final int limit;
    private final long offset;

    private HomoQuery(T criteria, List<String> columns, HomoSort sort, int limit, long offset) {
        this.criteria = criteria;
        this.columns = columns;
        this.sort = sort;
        this.limit = limit;
        this.offset = offset;
    }

    public static <T extends HomoCriteriaDefinition<T>> HomoQuery<T> query(T criteria){
        return new HomoQuery<T>(criteria);
    }

    private HomoQuery(T criteria){
        this(criteria, Collections.emptyList(),HomoSort.unsorted(),DEFAULT_LIMIT,DEFAULT_OFFSET);
    }

    public HomoQuery<T> withColumns(String... columns){
        Assert.notEmpty(columns,"Columns must not be null");
        List<String> newColumns = new ArrayList<>(this.columns);
        return new HomoQuery<T>(this.criteria, newColumns,this.sort,this.limit,this.offset);
    }

    public HomoQuery<T> offset(long offset) {
        return new HomoQuery<T>(this.criteria, this.columns, this.sort, this.limit, offset);
    }

    public HomoQuery<T> limit(int limit) {
        return new HomoQuery<T>(this.criteria, this.columns, this.sort, limit, this.offset);
    }
    public HomoQuery<T> sort(HomoSort sort) {
        Assert.notNull(sort, "Sort must not be null!");
        return new HomoQuery<T>(this.criteria, this.columns, this.sort.and(sort), this.limit, this.offset);
    }
}
