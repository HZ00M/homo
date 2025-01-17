package com.homo.relational.driver.mysql.covert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalTime;

@Component
public class TimeToLocalTime implements Converter<Time, LocalTime>{
    @Override
    public LocalTime convert(Time source) {
        return source.toLocalTime();
    }
}
