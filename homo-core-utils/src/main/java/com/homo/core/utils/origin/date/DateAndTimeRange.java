package com.homo.core.utils.origin.date;


import com.homo.core.utils.origin.DateTimeUtil;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateAndTimeRange implements Serializable {
    private static final long serialVersionUID = -6223187991219199011L;
    public final LocalDate date;
    public final LocalDateTime start,end;
    public final int intdate;
    public DateAndTimeRange(LocalDate date, LocalTime starTime, LocalTime endTime) {
        this.date = date;
        this.start=LocalDateTime.of(date,starTime);
        this.end=LocalDateTime.of(date,endTime);
        this.intdate= DateTimeUtil.formatDateToInt(date);
    }

}
