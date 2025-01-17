package com.homo.relational.driver.mysql.covert;

import com.alibaba.fastjson.JSON;
import com.homo.core.facade.relational.mapping.HomoJsonColumn;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class StringToHomoJsonColumnConvert implements ConditionalGenericConverter {
    @Override
    public boolean matches(TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        return sourceType.getType().isAssignableFrom(String.class) && HomoJsonColumn.class.isAssignableFrom(targetType.getType());
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, HomoJsonColumn.class));
    }

    @Override
    public Object convert(Object source, @NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        String sourceStr = (String) source;
        if (sourceStr.isEmpty()) {
            return null;
        }
        return JSON.parseObject(sourceStr, targetType.getType());
    }
}
