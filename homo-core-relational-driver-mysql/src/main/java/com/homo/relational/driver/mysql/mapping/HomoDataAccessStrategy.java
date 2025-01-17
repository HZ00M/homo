package com.homo.relational.driver.mysql.mapping;

import com.homo.core.facade.relational.mapping.HomoTable;
import com.homo.relational.driver.mysql.utils.MapperUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter;
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.StringUtils;

public class HomoDataAccessStrategy extends DefaultReactiveDataAccessStrategy {
    public HomoDataAccessStrategy(R2dbcDialect dialect, CustomConversions conversions) {
        super(dialect, createHomoConverter(dialect, conversions));
    }
    private static HomoMappingR2dbcConverter createHomoConverter(R2dbcDialect dialect, CustomConversions conversions) {
        return new HomoMappingR2dbcConverter(dialect, conversions);
    }
    public static class HomoMappingR2dbcConverter extends MappingR2dbcConverter {
        public HomoMappingR2dbcConverter(R2dbcDialect dialect, CustomConversions conversions) {
            super(new HomoR2dbcMappingContext(), conversions);
            MapperUtil.initialize(dialect,this);
        }
    }

    public static class HomoR2dbcMappingContext extends R2dbcMappingContext {
        public HomoR2dbcMappingContext(){
            super(new HomoNamingStrategy());
        }
        @Override
        protected <T> @NotNull RelationalPersistentEntity<T> createPersistentEntity(@NotNull TypeInformation<T> typeInformation) {
            HomoRelationalPersistentEntity<T> entity = new HomoRelationalPersistentEntity<>(typeInformation,getNamingStrategy(),isForceQuote());
            return entity;
        }

        @Override
        protected @NotNull RelationalPersistentProperty createPersistentProperty(@NotNull Property property, @NotNull RelationalPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
            HomoBasicRelationalPersistentProperty persistentProperty = new HomoBasicRelationalPersistentProperty(property, owner, simpleTypeHolder, getNamingStrategy());
            persistentProperty.setForceQuote(this.isForceQuote());
            return persistentProperty;
        }

    }

    public static class HomoNamingStrategy implements NamingStrategy{
        @Override
        public @NotNull String getTableName(Class<?> type) {
            HomoTable homoTable = type.getAnnotation(HomoTable.class);
            if (homoTable !=null && StringUtils.hasText(homoTable.value())){
                return homoTable.value();
            }
            return type.getSimpleName();
        }

        @Override
        public @NotNull String getColumnName(RelationalPersistentProperty property) {
            return property.getName();
        }

    }
}
