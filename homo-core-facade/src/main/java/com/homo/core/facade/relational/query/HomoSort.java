package com.homo.core.facade.relational.query;

import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomoSort {
    public static final HomoSort UNSORTED = by();
    public static final Direction DEFAULT_DIRECTION = Direction.ASC;
    @Getter
    private final List<Order> orders;

    protected HomoSort(List<Order> orders) {
        this.orders = orders;
    }

    public static HomoSort by(HomoSort.Order... order) {
        return new HomoSort(new ArrayList<>(Arrays.asList(order)));
    }

    public static HomoSort unsorted() {
        return UNSORTED;
    }

    public boolean isSorted() {
        return !orders.isEmpty();
    }

    public boolean isUnsorted() {
        return !isSorted();
    }

    public HomoSort and(HomoSort sort) {
        List<Order> these = new ArrayList<>(this.orders);
        these.addAll(sort.orders);
        return new HomoSort(these);
    }

    public enum Direction {
        ASC, DESC
    }

    @Getter
    public static class Order {
        private final Direction direction;
        private final String property;
        private final boolean ignoreCase;

        public Order(@Nullable Direction direction, String property) {
            this(direction, property, false);
        }

        private Order(@Nullable Direction direction, String property, boolean ignoreCase) {
            if (!StringUtils.hasText(property)) {
                throw new IllegalArgumentException("Property must not null or empty!");
            }

            this.direction = direction == null ? Direction.ASC : direction;
            this.property = property;
            this.ignoreCase = ignoreCase;
        }

        public static Order by(String property) {
            return new Order(DEFAULT_DIRECTION, property);
        }

        public static Order asc(String property) {
            return new Order(Direction.ASC, property);
        }

        public static Order desc(String property) {
            return new Order(Direction.DESC, property);
        }

        public Order direct(Direction direction) {
            return new Order(direction, property, ignoreCase);
        }

        public Order property(String property) {
            return new Order(direction, property, ignoreCase);
        }

        public Order ignoreCase() {
            return new Order(direction, property, true);
        }

    }


}
