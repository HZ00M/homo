package com.homo.relational.base.criteria;

import com.homo.core.facade.relational.query.criteria.CriteriaStep;
import com.homo.core.facade.relational.query.criteria.HomoCriteriaDefinition;
import com.homo.core.utils.lang.Pair;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HomoCriteria implements HomoCriteriaDefinition<HomoCriteria> {
    static final HomoCriteria EMPTY = new HomoCriteria(null, Comparator.INITIAL, null);
    private String column;
    private final @Nullable HomoCriteria previous;
    private final @Nullable Comparator comparator;
    private final @Nullable Object value;
    private List<HomoCriteria> group;
    private Combinator combinator;
    private boolean ignoreCase;

    private HomoCriteria(String column, @Nullable Comparator comparator, @Nullable Object value) {
        this(column, null, comparator, value, Collections.emptyList(), Combinator.INITIAL, false);
    }

    private HomoCriteria(String column, @Nullable HomoCriteria previous, @Nullable Comparator comparator, @Nullable Object value,
                         List<HomoCriteria> group, Combinator combinator) {
        this(column, previous, comparator, value, group, combinator, false);
    }

    private HomoCriteria(String column, HomoCriteria previous, @Nullable Comparator comparator, @Nullable Object value,
                         List<HomoCriteria> group, Combinator combinator, boolean ignoreCase) {
        this.column = column;
        this.previous = previous;
        this.comparator = comparator;
        this.value = value;
        this.group = group;
        this.combinator = combinator;
        this.ignoreCase = ignoreCase;
    }

    public static HomoCriteria empty() {
        return EMPTY;
    }

    public static HomoCriteria from(HomoCriteria... criteria) {

        Assert.notNull(criteria, "criteria must not be null");
        Assert.noNullElements(criteria, "criteria must not contain null elements");

        return from(Arrays.asList(criteria));
    }

    public static HomoCriteria from(List<HomoCriteria> criteriaList) {
        Assert.notNull(criteriaList, "criteriaList must not be null");
        Assert.noNullElements(criteriaList, "criteriaList must not contain null elements");
        if (criteriaList.isEmpty()) {
            return EMPTY;
        }
        if (criteriaList.size() == 1) {
            return criteriaList.get(0);
        }
        return EMPTY.and(criteriaList);
    }

    public static CriteriaStep<HomoCriteria> where(String column) {
        Assert.hasText(column, "column name must not be null or empty!");
        return new DefaultCriteriaStep(column);
    }

    public CriteriaStep<HomoCriteria> and(String column) {
        Assert.hasText(column, "column name must not be null or empty!");
        return new DefaultCriteriaStep(column) {
            @Override
            protected HomoCriteria createCriteria(Comparator comparator, @Nullable Object value) {
                return new HomoCriteria(column, HomoCriteria.this, comparator, value, Collections.emptyList(), Combinator.AND, false);
            }
        };
    }

    public HomoCriteria and(HomoCriteria criteria) {
        Assert.notNull(criteria, "criteria must not be null!");
        return and(Collections.singletonList(criteria));
    }

//    @SuppressWarnings("unchecked")
    public HomoCriteria and(List<HomoCriteria> criteriaList) {
        Assert.notNull(criteriaList, "criteriaList must not be null!");
        return new HomoCriteria(null, this, null, null, criteriaList, Combinator.AND, false);
    }

    public CriteriaStep<HomoCriteria> or(String column) {
        Assert.hasText(column, "column name must not be null or empty!");
        return new DefaultCriteriaStep(column) {
            @Override
            protected HomoCriteria createCriteria(Comparator comparator, @Nullable Object value) {
                return new HomoCriteria(column, HomoCriteria.this, comparator, value, Collections.emptyList(), Combinator.OR, false);
            }
        };
    }

    public HomoCriteria or(HomoCriteria criteria) {
        Assert.notNull(criteria, "criteria must not be null!");
        return or(Collections.singletonList(criteria));
    }

//    @SuppressWarnings("unchecked")
    public HomoCriteria or(List<HomoCriteria> criteriaList) {
        Assert.notNull(criteriaList, "criteriaList must not be null!");
        return new HomoCriteria(null, this, null, null, criteriaList, Combinator.OR, false);
    }

    public HomoCriteria ignoreCase(boolean ignoreCase) {
        if (this.ignoreCase != ignoreCase) {
            return new HomoCriteria(column, previous, comparator, value, group, combinator, ignoreCase);
        }
        return this;
    }


    @Override
    public boolean isEmpty() {
        if (!checkSelfIsEmpty()) {
            return false;
        }
        HomoCriteria previous = this.previous;
        while (previous != null) {
            if (!previous.checkSelfIsEmpty()) {
                return false;
            }
            previous = previous.previous;
        }
        return true;
    }

    private boolean checkSelfIsEmpty() {
        if (this.comparator == Comparator.INITIAL) {
            return true;
        }
        if (this.column != null) {
            return false;
        }
        for (HomoCriteria criteria : group) {
            if (!criteria.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasPrevious() {
        return !previous.isEmpty();
    }

    @Override
    public HomoCriteria getPrevious() {
        return previous;
    }

    @Override
    public Combinator getCombinator() {
        return combinator;
    }

    @Override
    public Comparator getComparator() {
        return comparator;
    }

    @Override
    public boolean isGroup() {
        return !this.group.isEmpty();
    }

    @Override
    public List<HomoCriteria> getGroup() {
        return group;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getColumn() {
        return column;
    }

    static class DefaultCriteriaStep implements CriteriaStep<HomoCriteria> {
        private final String property;

        DefaultCriteriaStep(String property) {
            this.property = property;
        }

        protected HomoCriteria createCriteria(Comparator comparator, @Nullable Object value) {
            return new HomoCriteria(this.property, comparator, value);
        }

        @Override
        public HomoCriteria eq(Object value) {
            Assert.notNull(value, "Value must not be null!");
            return createCriteria(Comparator.EQ, value);
        }

        @Override
        public HomoCriteria notEq(Object value) {
            Assert.notNull(value, "Value must not be null!");
            return createCriteria( Comparator.NEQ, value);
        }

        @Override
        public HomoCriteria in(Object... values) {
            Assert.notNull(values, "Values must not be null!");
            Assert.noNullElements(values, "Values must not contain a null value!");

            if (values.length > 0 && values[0] instanceof Collection) {
                throw new RuntimeException(
                        "You can only pass in one argument of type " + values[1].getClass().getName());
            }
            return createCriteria(Comparator.IN, Arrays.asList(values));
        }

        @Override
        public HomoCriteria notIn(Object... values) {
            Assert.notNull(values, "Values must not be null!");
            Assert.noNullElements(values, "Values must not contain a null value!");

            if (values.length > 0 && values[0] instanceof Collection) {
                throw new RuntimeException(
                        "You can only pass in one argument of type " + values[1].getClass().getName());
            }

            return createCriteria(Comparator.NOT_IN, Arrays.asList(values));
        }

        @Override
        public HomoCriteria between(Object begin, Object end) {
            Assert.notNull(begin, "Begin value must not be null!");
            Assert.notNull(end, "End value must not be null!");
            return createCriteria(Comparator.BETWEEN, Pair.of(begin, end));
        }

        @Override
        public HomoCriteria notBetween(Object begin, Object end) {
            Assert.notNull(begin, "Begin value must not be null!");
            Assert.notNull(end, "End value must not be null!");

            return createCriteria(Comparator.NOT_BETWEEN, Pair.of(begin, end));
        }

        @Override
        public HomoCriteria like(Object value) {
            Assert.notNull(value, "Value must not be null!");

            return createCriteria(Comparator.LIKE, value);
        }

        @Override
        public HomoCriteria notLike(Object value) {
            Assert.notNull(value, "Value must not be null!");
            return createCriteria(Comparator.NOT_LIKE, value);
        }

        @Override
        public HomoCriteria lessThan(Object value) {
            Assert.notNull(value, "Value must not be null!");

            return createCriteria(Comparator.LT, value);
        }

        @Override
        public HomoCriteria lessThanOrEquals(Object value) {
            Assert.notNull(value, "Value must not be null!");

            return createCriteria(Comparator.LE, value);
        }

        @Override
        public HomoCriteria greaterThan(Object value) {
            Assert.notNull(value, "Value must not be null!");

            return createCriteria(Comparator.GT, value);
        }

        @Override
        public HomoCriteria greaterThanOrEquals(Object value) {
            Assert.notNull(value, "Value must not be null!");

            return createCriteria(Comparator.GE, value);
        }

        @Override
        public HomoCriteria isNull() {
            return createCriteria(Comparator.IS_NULL, null);
        }

        @Override
        public HomoCriteria isNotNull() {
            return createCriteria(Comparator.IS_NOT_NULL, null);
        }

        @Override
        public HomoCriteria isTrue() {
            return createCriteria(Comparator.IS_TRUE, true);
        }

        @Override
        public HomoCriteria isFalse() {
            return createCriteria(Comparator.IS_FALSE, false);
        }
    }
}
