package com.homo.relational.driver.mysql.mapping;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.UnaryOperator;

public class HomoDerivedSqlIdentifier implements SqlIdentifier {
    private final String name;
    private final boolean quoted;

    HomoDerivedSqlIdentifier(String name, boolean quoted) {
        Assert.hasText(name, "A database object must have at least on name part.");
        this.name = name;
        this.quoted = quoted;
    }

    @Override
    public String getReference(IdentifierProcessing processing) {
        return this.name;
    }

    @Override
    public String toSql(IdentifierProcessing processing) {
        return quoted ? processing.quote(name) : name;
    }

    @Override
    public SqlIdentifier transform(UnaryOperator<String> transformationFunction) {
        Assert.notNull(transformationFunction, "Transformation function must not be null");
        return new HomoDerivedSqlIdentifier(transformationFunction.apply(name), quoted);
    }

    @NotNull
    @Override
    public Iterator<SqlIdentifier> iterator() {
        //显式泛型方法调用语法，用于在调用泛型方法时明确指定方法的泛型类型参数
        return Collections.<SqlIdentifier>singleton(this).iterator();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o instanceof SqlIdentifier) {
            return toString().equals(o.toString());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return quoted ? toSql(IdentifierProcessing.ANSI) : this.name;
    }
}
