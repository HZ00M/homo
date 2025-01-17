package com.homo.relational.base.aggregate.op;

import com.homo.core.facade.relational.aggregate.AggregationOp;
import com.homo.core.facade.relational.query.criteria.HomoCriteriaDefinition;
import lombok.Data;
import org.springframework.util.Assert;

@Data
public class MatchOp implements AggregationOp {
    private final HomoCriteriaDefinition criteriaDefinition;

    public MatchOp(HomoCriteriaDefinition criteriaDefinition){
        Assert.notNull(criteriaDefinition, "Criteria must not be null!");
        this.criteriaDefinition = criteriaDefinition;
    }

    @Override
    public OpType getOpType() {
        return OpType.MATCH;
    }
}
