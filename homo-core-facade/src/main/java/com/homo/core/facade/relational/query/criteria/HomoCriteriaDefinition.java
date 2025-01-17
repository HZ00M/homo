package com.homo.core.facade.relational.query.criteria;

import java.util.List;

public interface HomoCriteriaDefinition<T extends HomoCriteriaDefinition<T>> {

    boolean isEmpty();

    boolean hasPrevious();

    T getPrevious();

    Combinator getCombinator();

    Comparator getComparator();

    boolean isGroup();

    List<T> getGroup();

    Object getValue();

    String getColumn();

    enum Combinator {
        INITIAL, AND, OR
    }
    enum Comparator {
        INITIAL(""),EQ("="),NEQ("!="),
        BETWEEN("BETWEEN"),NOT_BETWEEN("NOT BETWEEN"),
        LT("<"),GT(">"),LE("<="),GE(">="),
        LIKE("%LIKE%"),NOT_LIKE("NOT LIKE%"),IN("IN"),NOT_IN("NOT IN"),
        IS_NULL("IS NULL"),IS_NOT_NULL("IS NOT NULL"),
        IS_TRUE("IS TRUE"), IS_FALSE("IS FALSE")
        ;
        private final String comparator;

        Comparator(String comparator) {
            this.comparator = comparator;
        }

        public String getComparator() {
            return comparator;
        }
    }
}
