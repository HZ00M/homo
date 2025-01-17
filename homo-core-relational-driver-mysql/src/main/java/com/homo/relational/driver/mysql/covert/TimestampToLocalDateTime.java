package com.homo.relational.driver.mysql.covert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class TimestampToLocalDateTime implements Converter<Timestamp, LocalDateTime> {
    @Override
    public LocalDateTime convert(Timestamp source) {
        return source.toLocalDateTime();
    }
}
