package com.homo.relational.driver.mysql.covert;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Set;

@Component
public class LocalTimeToTime implements ConditionalGenericConverter {
    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return sourceType.getType().equals(LocalTime.class) && targetType.getType().equals(Time.class);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.emptySet();
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null)
            return null;
        LocalTime localTime = (LocalTime) source;
        return Time.valueOf(localTime);
    }
}
