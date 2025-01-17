package com.homo.relational.driver.mysql.mapping;

import com.homo.core.facade.relational.mapping.HomoColumn;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.util.Lazy;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;

public class HomoBasicRelationalPersistentProperty extends HomoAnnotationPersistentProperty<RelationalPersistentProperty> implements RelationalPersistentProperty {
    private final Lazy<SqlIdentifier> columnName;
    @Getter
    @Setter
    private boolean forceQuote;

    public HomoBasicRelationalPersistentProperty(Property property, PersistentEntity<?, RelationalPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder, NamingStrategy namingStrategy) {
        super(property, owner, simpleTypeHolder);
        this.forceQuote = true;
        Assert.notNull(namingStrategy, "NamingStrategy must not be null.");
        this.columnName = Lazy.of(() -> Optional.ofNullable(this.findAnnotation(HomoColumn.class)).map(HomoColumn::value).filter(StringUtils::hasText).map(this::createSqlIdentifier).orElseGet(() -> this.createDerivedSqlIdentifier(namingStrategy.getColumnName(this))));
    }

    private SqlIdentifier createSqlIdentifier(String name) {
        return forceQuote ? SqlIdentifier.quoted(name) : SqlIdentifier.unquoted(name);
    }

    private SqlIdentifier createDerivedSqlIdentifier(String name) {
        return new HomoDerivedSqlIdentifier(name, forceQuote);
    }

    @Override
    public @NotNull RelationalPersistentEntity<?> getOwner() {
        return (RelationalPersistentEntity<?>) super.getOwner();
    }

    @Override
    protected Association<RelationalPersistentProperty> createAssociation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public SqlIdentifier getColumnName() {
        return this.columnName.get();
    }

    @Override
    public SqlIdentifier getReverseColumnName(PersistentPropertyPathExtension path) {
        return null;
    }

    @Override
    public SqlIdentifier getKeyColumn() {
        return null;
    }

    @Override
    public boolean isQualified() {
        return true;
    }

    @Override
    public Class<?> getQualifierColumnType() {
        Assert.isTrue(isQualified(), "The qualifier column type is only defined for properties that are qualified");

        if (isMap()) {
            return getTypeInformation().getRequiredComponentType().getType();
        }

        // for lists and arrays
        return Integer.class;
    }

    @Override
    public boolean isOrdered() {
        return isListLike();
    }

    @Override
    public boolean isEmbedded() {
        return RelationalPersistentProperty.super.isEmbedded();
    }

    @Override
    public String getEmbeddedPrefix() {
        return RelationalPersistentProperty.super.getEmbeddedPrefix();
    }

    @Override
    public boolean shouldCreateEmptyEmbedded() {
        return false;
    }
    private boolean isListLike() {
        return isCollectionLike() && !Set.class.isAssignableFrom(this.getType());
    }


}
