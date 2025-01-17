package org.springframework.data.relational.core.sql;

import org.springframework.util.StringUtils;

import java.util.List;

public class GroupBy extends AbstractSegment {
    private final List<Expression> groupByList;

    public GroupBy(List<Expression> selectList) {
        super(selectList.toArray(new Expression[0]));
        this.groupByList = selectList;
    }

    @Override
    public String toString() {
        return StringUtils.collectionToDelimitedString(groupByList, ", ");
    }
}

