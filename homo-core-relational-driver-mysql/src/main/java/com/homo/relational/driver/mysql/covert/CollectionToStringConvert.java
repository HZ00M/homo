package com.homo.relational.driver.mysql.covert;

import com.alibaba.fastjson.JSON;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 使用fastjson 把 Collection 转换为 String
 */
@Component
public class CollectionToStringConvert implements Converter<Collection<?>, String> {

    @Override
    public String convert(@NotNull Collection<?> source) {
        return JSON.toJSONString(source);
    }
}
