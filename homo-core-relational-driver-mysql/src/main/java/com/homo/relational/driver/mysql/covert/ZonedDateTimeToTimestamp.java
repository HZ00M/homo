package com.homo.relational.driver.mysql.covert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
@Component
public class ZonedDateTimeToTimestamp implements Converter<ZonedDateTime, Timestamp> {


    @Override
    public Timestamp convert(ZonedDateTime source) {
        if (source == null)
            return null;
        return Timestamp.valueOf(source.toLocalDateTime());
    }
}
