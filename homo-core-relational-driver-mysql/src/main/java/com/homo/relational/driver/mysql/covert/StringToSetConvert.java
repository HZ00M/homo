package com.homo.relational.driver.mysql.covert;

import com.alibaba.fastjson.JSON;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 使用fastjson 把 String 转换为 Set
 */
@Component
public class StringToSetConvert implements ConditionalGenericConverter {

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            // 判断sourceType是否是String类型，targetType是否是Set类型
            if (!sourceType.getType().isAssignableFrom(String.class)) {
                return false;
            }
            return targetType.isCollection();
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            // 返回转换的类型
            return Collections.singleton(new ConvertiblePair(String.class, Set.class));
        }

        @Override
        public Object convert(Object source, @NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
            // 转换逻辑
            // 使用fastjson将String转换为Set
            if (source == null) {
                return null;
            }
            String sourceStr = (String) source;
            if (!StringUtils.hasText(sourceStr)) {
                return null;
            }
            List<?> datas = JSON.parseArray(sourceStr, targetType.getElementTypeDescriptor().getType());
            return new HashSet<>(datas);
        }
}
