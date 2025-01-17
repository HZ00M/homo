package com.homo.relational.driver.mysql.covert;

import com.alibaba.fastjson.JSON;
import com.homo.core.facade.relational.mapping.HomoJsonColumn;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 使用fastjson 把 TpfJsonColumn 转换为 String
 */
@Component
public class HomoJsonColumnToStringConvert implements Converter<HomoJsonColumn, String> {
        @Override
        public String convert(@NotNull HomoJsonColumn source) {
            return JSON.toJSONString(source);
        }
}
