package com.homo.relational.driver.mysql.covert;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

@Component
public class LocalDateToDate implements ConditionalGenericConverter {

    @Override
    public boolean matches(TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        if (!sourceType.getType().equals(LocalDate.class)) {
            return false;
        }
        return targetType.getType().equals(Date.class);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.emptySet();
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null)
            return null;
        LocalDate localDate = (LocalDate) source;
        return Date.valueOf(localDate);
    }
}
