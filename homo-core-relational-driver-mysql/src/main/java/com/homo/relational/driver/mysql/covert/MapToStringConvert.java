package com.homo.relational.driver.mysql.covert;

import com.alibaba.fastjson.JSON;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MapToStringConvert implements Converter<Map<?, ?>, String> {
    // 使用fastjson将Map转换为String
    @Override
    public String convert(Map<?, ?> source) {
        return JSON.toJSONString(source);
    }
}
