package com.homo.relational.driver.mysql.covert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;

@Component
public class DateToLocalDate implements Converter<Date, LocalDate> {
    @Override
    public LocalDate convert(Date source) {
        return source.toLocalDate();
    }
}
