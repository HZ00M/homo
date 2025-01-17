package com.homo.core.facade.relational.query.criteria;

public interface CriteriaStep<T extends HomoCriteriaDefinition> {
    T eq(Object value);

    T notEq(Object value);

    T in(Object... value);

    T notIn(Object... value);

    T between(Object begin, Object end);

    T notBetween(Object begin, Object end);

    T like(Object value);

    T notLike(Object value);

    T lessThan(Object value);

    T lessThanOrEquals(Object value);

    T greaterThan(Object value);

    T greaterThanOrEquals(Object value);

    T isNull();

    T isNotNull();

    T isTrue();

    T isFalse();
}
