package com.homo.relational.driver.mysql.covert;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Component
public class LocalDateTimeToTimestamp implements ConditionalGenericConverter {

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return sourceType.getType().equals(LocalDateTime.class) && targetType.getType().equals(Timestamp.class);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.emptySet();
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null)
            return null;
        LocalDateTime localDateTime = (LocalDateTime) source;
        return Timestamp.valueOf(localDateTime);
    }
}
