package com.homo.relational.driver.mysql.utils;

import com.homo.core.facade.relational.query.HomoQuery;
import com.homo.core.facade.relational.query.HomoSort;
import com.homo.core.facade.relational.query.HomoUpdate;
import com.homo.core.facade.relational.query.criteria.HomoCriteriaDefinition;
import com.homo.relational.base.criteria.HomoCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class QueryConvertUtil {
    static Constructor<Criteria> criteriaConstructor;

    static {
        try {
            criteriaConstructor = Criteria.class.getDeclaredConstructor(Criteria.class, CriteriaDefinition.Combinator.class, List.class, SqlIdentifier.class, CriteriaDefinition.Comparator.class, Object.class);
            criteriaConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            log.error("Criteria constructor not found!!!");
            throw new RuntimeException(e);
        }
    }

    public static Update convertToUpdate(HomoUpdate homoUpdate) {
        Map<SqlIdentifier, Object> assignments = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : homoUpdate.getAssignments().entrySet()) {
            SqlIdentifier column = SqlIdentifier.quoted(entry.getKey());
            Object value = entry.getValue();
            assignments.put(column, value);
        }
        return Update.from(assignments);
    }

    @SuppressWarnings("uncecked")
    public static Query convertToQuery(HomoQuery homoQuery) {
        HomoCriteria homoCriteria = (HomoCriteria) homoQuery.getCriteria();
        Criteria criteria = convertToCriteria(homoCriteria);
        Query query = Query.query(criteria);
        if (!homoQuery.getColumns().isEmpty()) {
            query = query.columns(homoQuery.getColumns());
        }
        if (homoQuery.getOffset() != HomoQuery.DEFAULT_OFFSET) {
            query = query.offset(homoQuery.getOffset());
        }
        if (homoQuery.getLimit() != HomoQuery.DEFAULT_LIMIT) {
            query = query.limit(homoQuery.getLimit());
        }
        if (homoQuery.getSort() != HomoSort.unsorted()) {
            Sort sort = convertToSort(homoQuery.getSort());
            query = query.sort(sort);
        }
        return query;
    }

    public static Sort convertToSort(HomoSort homoSort) {
        if (homoSort.isUnsorted()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (HomoSort.Order homoOrder : homoSort.getOrders()) {
            Sort.Order order = convertOrder(homoOrder);
            orders.add(order);
        }
        return Sort.by(orders);
    }

    private static Sort.Order convertOrder(HomoSort.Order homoOrder) {
        Sort.Order order = null;
        switch (homoOrder.getDirection()) {
            case ASC:
                order = Sort.Order.asc(homoOrder.getProperty());
                break;
            case DESC:
                order = Sort.Order.desc(homoOrder.getProperty());
                break;
        }
        return order;
    }

    public static Criteria convertToCriteria(HomoCriteria homoCriteria) {
        try {
            if (homoCriteria == null) {
                return null;
            }
            CriteriaDefinition.Combinator combinator = convertCombinator(homoCriteria.getCombinator());
            CriteriaDefinition.Comparator comparator = convertComparator(homoCriteria.getComparator());
            SqlIdentifier identifier = null;
            if (StringUtils.hasText(homoCriteria.getColumn())) {
                identifier = SqlIdentifier.quoted(homoCriteria.getColumn());
            }
            List<Criteria> groupList = Collections.emptyList();
            if (homoCriteria.isGroup()) {
                groupList = homoCriteria.getGroup().stream().map(QueryConvertUtil::convertToCriteria).collect(Collectors.toList());
            }
            Criteria parentCriteria = convertToCriteria(homoCriteria.getPrevious());
            return criteriaConstructor.newInstance(parentCriteria, combinator, groupList, identifier, comparator, homoCriteria.getValue());
        } catch (Exception e) {
            log.error("convertCriteria fail {}", homoCriteria, e);
            throw new RuntimeException(e);
        }
    }

    private static CriteriaDefinition.Combinator convertCombinator(HomoCriteriaDefinition.Combinator homoCombinator) {
        CriteriaDefinition.Combinator combinator = CriteriaDefinition.Combinator.INITIAL;
        switch (homoCombinator) {
            case INITIAL:
                break;
            case AND:
                combinator = CriteriaDefinition.Combinator.AND;
                break;
            case OR:
                combinator = CriteriaDefinition.Combinator.OR;
                break;
        }
        return combinator;
    }

    private static CriteriaDefinition.Comparator convertComparator(HomoCriteriaDefinition.Comparator homoComparator) {
        CriteriaDefinition.Comparator comparator = null;
        switch (homoComparator) {
            case INITIAL:
                comparator = CriteriaDefinition.Comparator.INITIAL;
                break;
            case EQ:
                comparator = CriteriaDefinition.Comparator.EQ;
                break;
            case NEQ:
                comparator = CriteriaDefinition.Comparator.NEQ;
                break;
            case BETWEEN:
                comparator = CriteriaDefinition.Comparator.BETWEEN;
                break;
            case NOT_BETWEEN:
                comparator = CriteriaDefinition.Comparator.NOT_BETWEEN;
                break;
            case LT:
                comparator = CriteriaDefinition.Comparator.LT;
                break;
            case LE:
                comparator = CriteriaDefinition.Comparator.LTE;
                break;
            case GT:
                comparator = CriteriaDefinition.Comparator.GT;
                break;
            case GE:
                comparator = CriteriaDefinition.Comparator.GTE;
                break;
            case IS_NULL:
                comparator = CriteriaDefinition.Comparator.IS_NULL;
                break;
            case IS_NOT_NULL:
                comparator = CriteriaDefinition.Comparator.IS_NOT_NULL;
                break;
            case LIKE:
                comparator = CriteriaDefinition.Comparator.LIKE;
                break;
            case NOT_LIKE:
                comparator = CriteriaDefinition.Comparator.NOT_LIKE;
                break;
            case NOT_IN:
                comparator = CriteriaDefinition.Comparator.NOT_IN;
                break;
            case IN:
                comparator = CriteriaDefinition.Comparator.IN;
                break;
            case IS_TRUE:
                comparator = CriteriaDefinition.Comparator.IS_TRUE;
                break;
            case IS_FALSE:
                comparator = CriteriaDefinition.Comparator.IS_FALSE;
                break;
        }
        return comparator;
    }


}
