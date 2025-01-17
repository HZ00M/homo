package com.homo.relational.driver.mysql.covert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
public class StringToMapConvert implements ConditionalGenericConverter {

    @Override
    public boolean matches(TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        // 判断sourceType是否是String类型，targetType是否是Map类型
        if (!sourceType.getType().isAssignableFrom(String.class)) {
            return false;
        }
        return targetType.isMap();
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        // 返回转换的类型
        return Collections.singleton(new ConvertiblePair(String.class, Map.class));
    }

    @Override
    public Object convert(Object source, @NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        // 转换逻辑
        // 使用fastjson将String转换为Map
        if (source == null) {
            return null;
        }
        String sourceStr = (String) source;
        if (!StringUtils.hasText(sourceStr)) {
            return null;
        }
        // TypeDescriptor to ParameterizedTypeReference
        // fastjson sting to map
        Type type = new ParameterizedTypeImpl(new Type[]{targetType.getMapKeyTypeDescriptor().getType(), targetType.getMapValueTypeDescriptor().getType()}, null, Map.class);
        return JSON.parseObject(sourceStr, type);
    }
}
