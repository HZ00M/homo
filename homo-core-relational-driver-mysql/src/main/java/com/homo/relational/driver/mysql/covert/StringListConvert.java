package com.homo.relational.driver.mysql.covert;

import com.alibaba.fastjson.JSON;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;


@Component
public class StringListConvert implements ConditionalGenericConverter {

    @Override
    public boolean matches(TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        // 判断sourceType是否是String类型，targetType是否是List类型
        if (!sourceType.getType().isAssignableFrom(String.class)) {
            return false;
        }
        return targetType.isCollection();
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        // 返回转换的类型
        return Collections.singleton(new ConvertiblePair(String.class, List.class));
    }

    @Override
    public Object convert(Object source, @NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        // 转换逻辑
        // 使用fastjson将String转换为List
        if (source == null) {
            return null;
        }
        String sourceStr = (String) source;
        if (!StringUtils.hasText(sourceStr)) {
            return null;
        }
        return JSON.parseArray(sourceStr, targetType.getElementTypeDescriptor().getType());
    }
}
