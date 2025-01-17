package com.homo.relational.driver.mysql.mapping;

import com.homo.core.facade.relational.mapping.HomoTable;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.util.Lazy;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class HomoRelationalPersistentEntity<T> extends BasicPersistentEntity<T, RelationalPersistentProperty> implements RelationalPersistentEntity<T> {
    private final NamingStrategy namingStrategy;
    private final Lazy<Optional<SqlIdentifier>> tableName;
    private boolean forceQuote = true;

    public HomoRelationalPersistentEntity(TypeInformation<T> typeInformation, NamingStrategy namingStrategy,boolean forceQuote) {
        super(typeInformation);
        this.namingStrategy = namingStrategy;
        this.tableName = Lazy.of(() -> Optional.ofNullable(findAnnotation(HomoTable.class))
                .map(HomoTable::value)
                .filter(StringUtils::hasText)
                .map(this::createSqlIdentifier)
        );
        this.forceQuote = forceQuote;
    }

    private SqlIdentifier createSqlIdentifier(String name) {
        return SqlIdentifier.quoted(name);
    }

    private SqlIdentifier createDerivedSqlIdentifier(String name) {
        return new HomoDerivedSqlIdentifier(name, forceQuote);
    }

    @Override
    public SqlIdentifier getTableName() {
        return tableName.get().orElseGet(() -> {
            String schema = namingStrategy.getSchema();
            SqlIdentifier tableNameIdentifier = createDerivedSqlIdentifier(namingStrategy.getTableName(getType()));
            return StringUtils.hasText(schema) ? SqlIdentifier.from(createDerivedSqlIdentifier(schema), tableNameIdentifier) : tableNameIdentifier;
        });
    }

    @Override
    public SqlIdentifier getIdColumn() {
        return getRequiredIdProperty().getColumnName();
    }
}
