package com.homo.core.utils.origin;

import com.homo.core.utils.origin.date.DatePattern;
import com.homo.core.utils.origin.exceptions.DateException;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 */
public final class DateTimeUtil {

    private DateTimeUtil() {
    }


    public final static ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    public final static ZoneOffset DEFAULT_ZONE_OFFSET = OffsetDateTime.now(DEFAULT_ZONE_ID).getOffset(); //系统默认偏移时区

    /**
     * @return 当前日期时间，含zoneId
     */
    public static ZonedDateTime nowZone() {
        return ZonedDateTime.now();
    }
    /**
     * @return 当前日期时间，含offset
     */
    public static OffsetDateTime nowOffset() {
        return OffsetDateTime.now();
    }
    /**
     * @return 当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * @return 当前日期
     */
    public static LocalDate date() {
        return LocalDate.now();
    }

    /**
     * 当前年月
     *
     * @return
     */
    public static YearMonth yearMonth() {
        return YearMonth.now();
    }

    /**
     * 当前月日
     *
     * @return
     */
    public static MonthDay monthDay() {
        return MonthDay.now();
    }

    /**
     * @return 当前时间
     */
    public static LocalTime time() {
        return LocalTime.now();
    }

    /**
     * @param offsetDateTime
     * @return 设置时间为零点零分零秒
     */
    public static OffsetDateTime withStartTime(OffsetDateTime offsetDateTime) {
        return OffsetDateTime.of(offsetDateTime.toLocalDate(), LocalTime.MIN,offsetDateTime.getOffset());
    }

    /**
     * @param localDateTime
     * @return 设置时间为零点零分零秒
     */
    public static LocalDateTime withStartTime(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.MIN);
    }

    /**
     * @param offsetDateTime
     * @return 设置时间为23点59分59秒
     */
    public static OffsetDateTime withEndTime(OffsetDateTime offsetDateTime) {
        return OffsetDateTime.of(offsetDateTime.toLocalDate(), LocalTime.MAX,offsetDateTime.getOffset());
    }
    /**
     * @param localDateTime
     * @return 设置时间为23点59分59秒
     */
    public static LocalDateTime withEndTime(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.MAX);
    }

    /**
     * @return 当天的开始时间。日期为当天，时间为零点零分零秒
     */
    public static OffsetDateTime nowStartOffset() {
        return withStartTime(OffsetDateTime.now());
    }

    /**
     * @return 当天的结束时间。日期为当天，时间为23点59分59秒
     */
    public static OffsetDateTime nowEndOffset() {
        return withEndTime(OffsetDateTime.now());
    }


    /**
     * @return 当天的开始时间。日期为当天，时间为零点零分零秒
     */
    public static LocalDateTime nowStart() {
        return withStartTime(LocalDateTime.now());
    }

    /**
     * @return 当天的结束时间。日期为当天，时间为23点59分59秒
     */
    public static LocalDateTime nowEnd() {
        return withEndTime(LocalDateTime.now());
    }

    /**
     * @return 当前瞬时 UTC标准
     */
    public static Instant instant() {
        return Instant.now();
    }

    /**
     * 获取当前毫秒
     */
    public static long nowEpochMilli() {
        return Instant.now().toEpochMilli();
    }


    public static OffsetDateTime toOffsetDateTime(long timeMillis) {
        return toOffsetDateTime(Instant.ofEpochMilli(timeMillis));
    }

    public static OffsetDateTime toOffsetDateTime(Instant instant) {
        return OffsetDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
    }
    public static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return OffsetDateTime.of(localDateTime,DEFAULT_ZONE_OFFSET);
    }
    public static OffsetDateTime toOffsetDateTime(LocalDate localDate, LocalTime localTime) {
        return OffsetDateTime.of(localDate, localTime,DEFAULT_ZONE_OFFSET);
    }

    public static OffsetDateTime toOffsetDateTime(LocalDate localDate) {
        return OffsetDateTime.of(localDate, LocalTime.MIN,DEFAULT_ZONE_OFFSET);
    }

    public static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return toOffsetDateTime(timestamp.toInstant());
    }

    public static OffsetDateTime toOffsetDateTime(Date date) {
        return toOffsetDateTime(date.toInstant());
    }

    public static OffsetDateTime toOffsetDateTime(Calendar calendar) {
        return toOffsetDateTime(calendar.toInstant());
    }



    public static LocalDateTime toLocalDateTime(long timeMillis) {
        return toLocalDateTime(Instant.ofEpochMilli(timeMillis));
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
    }
    public static LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toLocalDateTime();
    }
    public static LocalDateTime toLocalDateTime(LocalDate localDate, LocalTime localTime) {
        return LocalDateTime.of(localDate, localTime);
    }

    public static LocalDateTime toLocalDateTime(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIN);
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return toLocalDateTime(date.toInstant());
    }

    public static LocalDateTime toLocalDateTime(Calendar calendar) {
        return toLocalDateTime(calendar.toInstant());
    }

    public static Instant toInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant();
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.toInstant(DEFAULT_ZONE_OFFSET);
    }

    public static Instant toInstant(LocalDate localDate, LocalTime localTime) {
        return toInstant(toLocalDateTime(localDate, localTime));
    }

    public static Instant toInstant(Timestamp timestamp) {
        return timestamp.toInstant();
    }

    public static Instant toInstant(Date date) {
        return date.toInstant();
    }

    public static Instant toInstant(Calendar calendar) {
        return calendar.toInstant();
    }

    public static Instant toInstant(TemporalAccessor temporalAccessor) {
        if (null == temporalAccessor) {
            return null;
        }

        Instant result;
        if (temporalAccessor instanceof Instant) {
            result = (Instant) temporalAccessor;
        } else if (temporalAccessor instanceof LocalDateTime) {
            result = ((LocalDateTime) temporalAccessor).atZone(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof ZonedDateTime) {
            result = ((ZonedDateTime) temporalAccessor).toInstant();
        } else if (temporalAccessor instanceof OffsetDateTime) {
            result = ((OffsetDateTime) temporalAccessor).toInstant();
        } else if (temporalAccessor instanceof LocalDate) {
            result = ((LocalDate) temporalAccessor).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof LocalTime) {
            // 指定本地时间转换 为Instant，取当天日期
            result = ((LocalTime) temporalAccessor).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof OffsetTime) {
            // 指定本地时间转换 为Instant，取当天日期
            result = ((OffsetTime) temporalAccessor).atDate(LocalDate.now()).toInstant();
        } else {
            result = Instant.from(temporalAccessor);
        }

        return result;
    }

    public static LocalDate toLocalDate(long timeMillis) {
        return toLocalDate(Instant.ofEpochMilli(timeMillis));
    }

    public static LocalDate toLocalDate(Instant instant) {
        return toLocalDateTime(instant).toLocalDate();
    }

    public static LocalDate toLocalDate(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toLocalDate();
    }

    public static LocalDate toLocalDate(Timestamp timestamp) {
        return timestamp.toLocalDateTime().toLocalDate();
    }

    public static LocalDate toLocalDate(Date date) {
        return toLocalDateTime(date).toLocalDate();
    }

    public static LocalDate toLocalDate(Calendar calendar) {
        return toLocalDateTime(calendar).toLocalDate();
    }

    public static LocalTime toLocalTime(long timeMillis) {
        return toLocalTime(Instant.ofEpochMilli(timeMillis));
    }

    public static LocalTime toLocalTime(Instant instant) {
        return toLocalDateTime(instant).toLocalTime();
    }

    public static LocalTime toLocalTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toLocalTime();
    }

    public static LocalTime toLocalTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime().toLocalTime();
    }

    public static LocalTime toLocalTime(Time time) {
        return time.toLocalTime();
    }

    public static LocalTime toLocalTime(Date date) {
        return toLocalDateTime(date).toLocalTime();
    }

    public static LocalTime toLocalTime(Calendar calendar) {
        return toLocalDateTime(calendar).toLocalTime();
    }

    public static YearMonth toYearMonth(OffsetDateTime offsetDateTime) {
        YearMonth yearMonth = YearMonth.of(offsetDateTime.getYear(),offsetDateTime.getMonth());
        return yearMonth;
    }

    public static YearMonth toYearMonth(LocalDate localDate) {
        YearMonth yearMonth = YearMonth.of(localDate.getYear(), localDate.getMonth());
        return yearMonth;
    }

    public static YearMonth toYearMonth(LocalDateTime localDateTime) {
        YearMonth yearMonth = YearMonth.of(localDateTime.getYear(), localDateTime.getMonth());
        return yearMonth;
    }

    public static YearMonth toYearMonth(long timeMillis) {
        return toYearMonth(Instant.ofEpochMilli(timeMillis));
    }

    public static YearMonth toYearMonth(Instant instant) {
        return toYearMonth(toLocalDateTime(instant));
    }

    public static YearMonth toYearMonth(Timestamp timestamp) {
        return toYearMonth(timestamp.toLocalDateTime());
    }

    public static YearMonth toYearMonth(Date date) {
        return toYearMonth(date.toInstant());
    }

    public static YearMonth toYearMonth(Calendar calendar) {
        return toYearMonth(calendar.toInstant());
    }

    public static MonthDay toMonthDay(OffsetDateTime offsetDateTime) {
        MonthDay monthDay = MonthDay.of(offsetDateTime.getMonth(), offsetDateTime.getDayOfMonth());
        return monthDay;
    }

    public static MonthDay toMonthDay(LocalDate localDate) {
        MonthDay monthDay = MonthDay.of(localDate.getMonth(), localDate.getDayOfMonth());
        return monthDay;
    }

    public static MonthDay toMonthDay(LocalDateTime localDateTime) {
        MonthDay monthDay = MonthDay.of(localDateTime.getMonth(), localDateTime.getDayOfMonth());
        return monthDay;
    }

    public static MonthDay toMonthDay(long timeMillis) {
        return toMonthDay(Instant.ofEpochMilli(timeMillis));
    }

    public static MonthDay toMonthDay(Instant instant) {
        return toMonthDay(toLocalDateTime(instant));
    }

    public static MonthDay toMonthDay(Timestamp timestamp) {
        return toMonthDay(timestamp.toLocalDateTime());
    }

    public static MonthDay toMonthDay(Date date) {
        return toMonthDay(date.toInstant());
    }

    public static MonthDay toMonthDay(Calendar calendar) {
        return toMonthDay(calendar.toInstant());
    }


    public static Timestamp nowTimestamp() {
        return Timestamp.from(Instant.now());
    }

    public static Timestamp toTimestamp(long timeMillis) {
        return new Timestamp(timeMillis);
    }

    public static Timestamp toTimestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    public static Timestamp toTimestamp(OffsetDateTime offsetDateTime) {
        return Timestamp.valueOf(offsetDateTime.toLocalDateTime());
    }

    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    public static Timestamp toTimestamp(LocalDate localDate, LocalTime localTime) {
        return Timestamp.valueOf(toLocalDateTime(localDate, localTime));
    }

    public static Timestamp toTimestamp(LocalDate localDate) {
        return Timestamp.valueOf(toLocalDateTime(localDate, LocalTime.MIN));
    }

    public static Timestamp toTimestamp(Date date) {
        return Timestamp.from(date.toInstant());
    }

    public static Timestamp toTimestamp(Calendar calendar) {
        return Timestamp.from(calendar.toInstant());
    }


    public static Date nowDate() {
        return new Date();
    }

    public static Date toDate(long timeMillis) {
        return new Date(timeMillis);
    }

    public static Date toDate(Instant instant) {
        return Date.from(instant);
    }

    public static Date toDate(OffsetDateTime offsetDateTime) {
        return toTimestamp(offsetDateTime);
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return toTimestamp(localDateTime);
    }

    public static Date toDate(LocalDate localDate, LocalTime localTime) {
        return toTimestamp(toLocalDateTime(localDate, localTime));
    }

    public static Date toDate(LocalDate localDate) {
        return toTimestamp(toLocalDateTime(localDate, LocalTime.MIN));
    }

    public static Date toDate(Timestamp timestamp) {
        return timestamp;
    }

    public static Date toDate(Calendar calendar) {
        return calendar.getTime();
    }

    public static Calendar nowCalendar() {
        return Calendar.getInstance();
    }

    public static Calendar toCalendar(long timeMillis) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeMillis);
        return cal;
    }

    public static Calendar toCalendar(Date date) {
        return toCalendar(date.getTime());
    }

    public static Calendar toCalendar(Instant instant) {
        return toCalendar(instant.toEpochMilli());
    }

    public static Calendar toCalendar(OffsetDateTime offsetDateTime) {
        return  toCalendar(getEpochMilli(offsetDateTime));
    }

    public static Calendar toCalendar(LocalDateTime dateTime) {
        return toCalendar(getEpochMilli(dateTime));
    }

    public static Calendar toCalendar(LocalDate localDate, LocalTime localTime) {
        return toCalendar(getEpochMilli(toLocalDateTime(localDate, localTime)));
    }

    public static Calendar toCalendar(LocalDate localDate) {
        return toCalendar(getEpochMilli(toLocalDateTime(localDate, LocalTime.MIN)));
    }

    public static Calendar toCalendar(Timestamp timestamp) {
        return toCalendar(timestamp.getTime());
    }

    // ------------------------------------ Format start ----------------------------------------------

    /**
     * 根据特定格式格式化日期时间
     *
     * @param date      被格式化的日期时间
     * @param formatter 日期格式，常用格式见： {@link DatePattern}
     * @return 格式化后的字符串
     */
    public static String format(Date date, DateTimeFormatter formatter) {
        return toLocalDateTime(date).format(formatter);
    }

    public static String format(Date date, String format) {
        return format(date, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 根据特定格式格式化日期时间
     *
     * @param temporal  被格式化的日期时间
     * @param formatter 日期格式，常用格式见： {@link DatePattern}
     * @return 格式化后的字符串
     */
    public static String format(TemporalAccessor temporal, DateTimeFormatter formatter) {
        return formatter.format(temporal);
    }

    public static String format(TemporalAccessor temporal, String format) {
        return format(temporal, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 格式化日期时间<br>
     * 格式 yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param localDateTime 被格式化的日期时间
     * @return 格式化后的日期
     */
    public static String formatDateTimeMs(LocalDateTime localDateTime) {

        return format(localDateTime, DatePattern.NORM_DATETIME_MS_PATTERN);
    }
    public static String formatDateTimeMs(OffsetDateTime offsetDateTime) {
        return format(offsetDateTime, DatePattern.NORM_DATETIME_MS_PATTERN);
    }
    public static String formatDateTimeMs(Date date) {
        return format(toLocalDateTime(date), DatePattern.NORM_DATETIME_MS_PATTERN);
    }

    public static String formatDateTimeMs(long timeMillis) {

        return formatDateTimeMs(toLocalDateTime(timeMillis));
    }

    /**
     * 格式化日期时间<br>
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param localDateTime 被格式化的日期时间
     * @return 格式化后的日期
     */
    public static String formatDateTime(LocalDateTime localDateTime) {

        return format(localDateTime, DatePattern.NORM_DATETIME_FORMATTER);
    }
    public static String formatDateTime(OffsetDateTime offsetDateTime) {

        return format(offsetDateTime, DatePattern.NORM_DATETIME_FORMATTER);
    }
    public static String formatDateTime(Date date) {
        return format(toLocalDateTime(date), DatePattern.NORM_DATETIME_FORMATTER);
    }

    public static String formatDateTime(long timeMillis) {
        return formatDateTime(toLocalDateTime(timeMillis));
    }

    /**
     * 格式化日期部分（不包括时间）<br>
     * 格式 yyyy-MM-dd
     *
     * @param localDate 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDate localDate) {
        return format(localDate, DatePattern.NORM_DATE_FORMATTER);
    }

    public static String formatDate(Date date) {
        return formatDate(toLocalDate(date));
    }

    public static String formatDate(long timeMillis) {
        return formatDate(toLocalDate(timeMillis));
    }

    /**
     * 格式化时间<br>
     * 格式 HH:mm:ss
     *
     * @param localTime 被格式化的时间
     * @return 格式化后的字符串
     */
    public static String formatTime(LocalTime localTime) {
        return format(localTime, DatePattern.NORM_TIME_FORMATTER);
    }
    public static String formatTime(OffsetTime offsetTime) {
        return format(offsetTime, DatePattern.NORM_TIME_FORMATTER);
    }
    public static String formatTime(Date date) {
        return formatTime(toLocalTime(date));
    }

    /**
     * 格式化日期<br>
     * 格式 yyyy-MM
     *
     * @param yearMonth 被格式化的年月
     * @return 格式化后的字符串
     */
    public static String formatYearMonth(YearMonth yearMonth) {
        return format(yearMonth, DatePattern.NORM_YEAR_MONTH_FORMATTER);
    }

    /**
     * 格式化月日<br>
     * 格式 MM-dd
     *
     * @param monthDay 被格式化的月日
     * @return 格式化后的字符串
     */
    public static String formatMonthDay(MonthDay monthDay) {
        return format(monthDay, DatePattern.NORM_MONTH_DAY_FORMATTER);

    }

    /**
     * 以整数的格式，格式化日期为字符串
     *
     * @param date
     * @return
     */
    public static String formatIntDate(Date date) {
        return format(date, DatePattern.INT_DATE_FORMATTER);
    }

    public static String formatIntDate(LocalDate localDate) {
        return format(localDate, DatePattern.INT_DATE_FORMATTER);
    }

    public static String formatIntTime(Date date) {
        return format(date, DatePattern.INT_TIME_FORMATTER);
    }

    public static String formatIntTime(LocalTime localTime) {
        return format(localTime, DatePattern.INT_TIME_FORMATTER);
    }


    public static String formatIntDateTime(Date date) {
        return format(date, DatePattern.LONG_DATETIME_FORMATTER);
    }

    public static String formatIntDateTime(LocalDateTime localDateTime) {
        return format(localDateTime, DatePattern.LONG_DATETIME_FORMATTER);
    }

    public static String formatIntYearMonth(Date date) {
        return format(date, DatePattern.INT_YEAR_MONTH_FORMATTER);
    }

    public static String formatIntYearMonth(YearMonth yearMonth) {
        return format(yearMonth, DatePattern.INT_YEAR_MONTH_FORMATTER);
    }

    public static String formatIntMonthDay(Date date) {
        return format(date, DatePattern.INT_MONTH_DAY_FORMATTER);
    }

    public static String formatIntMonthDay(MonthDay monthDay) {
        return format(monthDay, DatePattern.INT_MONTH_DAY_FORMATTER);
    }

    public static String formatIntDateMinute(Date date) {
        return format(date, DatePattern.LONG_SHORT_DATE_MINUTE_FORMATTER);
    }

    public static String formatIntDateMinute(LocalDateTime localDateTime) {
        return format(localDateTime, DatePattern.LONG_SHORT_DATE_MINUTE_FORMATTER);
    }

    /**
     * 将日期时间转为整数格式
     *
     * @return
     */
    public static int formatDateToInt(Date date) {
        return Integer.parseInt(formatIntDate(date));
    }

    public static int formatDateToInt(LocalDate localDate) {
        return Integer.parseInt(formatIntDate(localDate));
    }

    public static long formatDateMinuteToInt(Date date){
        return Long.parseLong(DatePattern.LONG_SHORT_DATE_MINUTE_FORMATTER.format(toLocalDateTime(date)));
    }
    public static long formatDateMinuteToInt(LocalDateTime date){
        String rs=DatePattern.LONG_SHORT_DATE_MINUTE_FORMATTER.format(date);
        return Long.parseLong(rs);
    }

    public static long formatDateTimeToInt(Date date) {
        return Long.parseLong(formatIntDateTime(date));
    }

    public static long formatDateTimeToInt(LocalDateTime localDateTime) {
        return Long.parseLong(formatIntDateTime(localDateTime));
    }

    public static int formatTimeToInt(LocalTime localTime) {
        return Integer.parseInt(formatIntTime(localTime));
    }
    public static int formatTimeToInt(Date date) {
        return Integer.parseInt(formatIntTime(date));
    }


    public static int formatYearMonthToInt(Date date) {
        return Integer.parseInt(formatIntYearMonth(date));
    }

    public static int formatYearMonthToInt(YearMonth yearMonth) {
        return Integer.parseInt(formatIntYearMonth(yearMonth));
    }

    public static int formatMonthDayToInt(Date date) {
        return Integer.parseInt(formatIntMonthDay(date));
    }

    public static int formatMonthDayToInt(MonthDay monthDay) {
        return Integer.parseInt(formatIntMonthDay(monthDay));
    }



    /**
     * 当前日期时间，格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 当前日期的标准形式字符串
     */
    public static String nowStr() {
        return formatDateTime(now());
    }

    /**
     * 当前日期，格式 yyyy-MM-dd
     *
     * @return 当前日期的标准形式字符串
     */
    public static String dateStr() {
        return formatDate(date());
    }

    /**
     * 当前时间，格式 HH:mm:ss
     *
     * @return 当前时间的标准形式字符串
     */
    public static String timeStr() {
        return formatTime(time());
    }

    /**
     * 当前年月
     *
     * @return 当前年月的标准形式字符串
     */
    public static String yearMonthStr() {
        return formatYearMonth(yearMonth());
    }

    /**
     * 当前月日
     *
     * @return 当前月日的标准形式字符串
     */
    public static String monthDayStr() {
        return formatMonthDay(monthDay());
    }


    /**
     * 当前日期时间，格式 yyyyMMddHHmmss
     *
     * @return 当前日期时间的整数形式
     */
    public static long nowInt() {
        return formatDateTimeToInt(now());
    }

    /**
     * 当前日期，格式 yyyy-MM-dd
     *
     * @return 当前日期的整数形式
     */
    public static int dateInt() {
        return formatDateToInt(date());
    }

    /**
     * 当前时间，格式 HH:mm:ss
     *
     * @return 当前时间的整数形式
     */
    public static int timeInt() {
        return formatTimeToInt(time());
    }

    /**
     * 当前年月
     *
     * @return 当前年月的整数形式
     */
    public static int yearMonthInt() {
        return formatYearMonthToInt(yearMonth());
    }

    /**
     * 当前月日
     *
     * @return 当前月日的整数形式
     */
    public static int monthDayInt() {
        return formatMonthDayToInt(monthDay());
    }

    // ------------------------------------ Format end ----------------------------------------------

    // ------------------------------------ Parse start ----------------------------------------------

    public static LocalDateTime parseDateTime(String dateStr, DateTimeFormatter formatter) {
        return LocalDateTime.parse(dateStr, formatter);
    }

    public static LocalDateTime parseDateTime(String dateStr, String format) {
        return parseDateTime(dateStr, DateTimeFormatter.ofPattern(format));
    }


    public static LocalDateTime parseDateTime(String dateStr) {
        return parseDateTime(dateStr, DatePattern.NORM_DATETIME_FORMATTER);
    }

    public static LocalDateTime parseLongDateTime(String dateStr) {
        return parseDateTime(dateStr, DatePattern.LONG_DATETIME_FORMATTER);
    }

    public static LocalDateTime parseLongDateTime(long dateInt) {
        return parseDateTime(Long.toString(dateInt), DatePattern.LONG_DATETIME_FORMATTER);
    }

    public static YearMonth parseYearMonth(String yyyymm, DateTimeFormatter formatter) {
        return YearMonth.parse(yyyymm, formatter);
    }

    public static YearMonth parseYearMonth(String yyyymm) {
        return parseYearMonth(yyyymm, DatePattern.NORM_YEAR_MONTH_FORMATTER);
    }

    public static YearMonth parseIntYearMonth(String yyyymm) {
        return parseYearMonth(yyyymm, DatePattern.INT_YEAR_MONTH_FORMATTER);
    }

    public static YearMonth parseIntYearMonth(int yyyymm) {
        return parseYearMonth(Integer.toString(yyyymm), DatePattern.INT_YEAR_MONTH_FORMATTER);
    }

    public static MonthDay parseMonthDay(String mmdd, DateTimeFormatter formatter) {
        return MonthDay.parse(mmdd, formatter);
    }

    public static MonthDay parseMonthDay(String mmdd) {
        return parseMonthDay(mmdd, DatePattern.NORM_MONTH_DAY_FORMATTER);
    }

    public static MonthDay parseIntMonthDay(String mmdd) {
        return parseMonthDay(mmdd, DatePattern.INT_MONTH_DAY_FORMATTER);
    }

    public static MonthDay parseIntMonthDay(int mmdd) {
        return parseMonthDay(Integer.toString(mmdd), DatePattern.INT_MONTH_DAY_FORMATTER);
    }

    public static LocalDate parseDate(String dateString, DateTimeFormatter formatter) {
        return LocalDate.parse(dateString, formatter);
    }

    public static LocalDate parseDate(String dateString) {
        return parseDate(dateString, DatePattern.NORM_DATE_FORMATTER);
    }

    public static LocalDate parseDate(String dateString, String format) {
        return parseDate(dateString, DateTimeFormatter.ofPattern(format));
    }

    public static LocalDate parseIntDate(String dateString) {
        return parseDate(dateString, DatePattern.INT_DATE_FORMATTER);
    }

    public static LocalDate parseIntDate(int dateInt) {
        return parseDate(Integer.toString(dateInt), DatePattern.INT_DATE_FORMATTER);
    }

    public static LocalTime parseTime(String dateString, DateTimeFormatter formatter) {
        return LocalTime.parse(dateString, formatter);
    }

    public static LocalTime parseTime(String timeString) {
        return parseTime(timeString, DatePattern.NORM_TIME_FORMATTER);
    }

    public static LocalTime parseTime(String timeString, String format) {
        return parseTime(timeString, DateTimeFormatter.ofPattern(format));
    }

    public static LocalTime parseIntTime(String timeString) {
        return parseTime(timeString, DatePattern.INT_TIME_FORMATTER);
    }

    public static LocalTime parseIntTime(int timeInt) {
        return parseTime(Integer.toString(timeInt), DatePattern.INT_TIME_FORMATTER);
    }

    private static DateTimeFormatter[] transDateMatchList = {
            DatePattern.NORM_DATE_FORMATTER,
            DatePattern.NORM_DATE_FORMATTER_SHORT,
            DatePattern.SLASH_DATE_FORMATTER,
            DatePattern.SLASH_DATE_FORMATTER_SHORT

    };
    /**
     * 按以下日期格式，尝试解析日期
     * 1. yyyy-MM-dd
     * 2. yyyy-M-d
     * 3. yyyy/MM/dd
     * 4. yyyy/M/d
     * @param dateString 需要解析的日期格式字符串
     * @return 失败抛出异常
     */
    public static LocalDate tryParseDate(String dateString) {
        for(DateTimeFormatter formatter: transDateMatchList){
            try{
                return LocalDate.parse(dateString,formatter);
            }catch (Exception ignore){
            }
        }
        throw new DateException("日期字符串格式不正确");
    }

    private static DateTimeFormatter[] transYearMonthMatchList = {
            DatePattern.NORM_YEAR_MONTH_FORMATTER,
            DatePattern.INT_YEAR_MONTH_FORMATTER
    };
    /**
     * 按以下日期格式，尝试解析年与月
     * 1. yyyy-MM
     * 2. yyyyMM
     * @param yearMonthString 需要解析的年月格式字符串
     * @return 失败抛出异常
     */
    public static YearMonth tryParseYearMonth(String yearMonthString) {
        for(DateTimeFormatter formatter: transYearMonthMatchList){
            try{
                return YearMonth.parse(yearMonthString,formatter);
            }catch (Exception ignore){
            }
        }
        throw new DateException("年月字符串格式不正确");
    }
    /**
     * 格式：<br>
     * <ol>
     * <li>yyyy-MM-dd HH:mm:ss</li>
     * <li>yyyy-MM-dd</li>
     * <li>HH:mm</li>
     * <li>HH:mm:ss</li>
     * <li>yyyy-MM-dd HH:mm</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * </ol>
     *
     * @param charSequence 日期字符串
     * @return 日期
     */
    public static LocalDateTime parse(CharSequence charSequence) {
        if (null == charSequence) {
            return null;
        }
        String dateStr = charSequence.toString().trim();
        int length = dateStr.length();
        try {
            if (length == DatePattern.NORM_DATETIME_PATTERN.length()) {
                return parseDateTime(dateStr);
            } else if (length == DatePattern.NORM_DATE_PATTERN.length()) {
                return parseDate(dateStr).atStartOfDay();
            } else if (length == DatePattern.NORM_MIN_TIME_PATTERN.length()) {
                return parseTime(dateStr, DatePattern.NORM_MIN_TIME_FORMATTER).atDate(LocalDate.now());
            } else if (length == DatePattern.NORM_TIME_PATTERN.length()) {
                return parseTime(dateStr).atDate(LocalDate.now());
            } else if (length == DatePattern.NORM_DATETIME_MINUTE_PATTERN.length()) {
                return parseDateTime(dateStr, DatePattern.NORM_DATETIME_MINUTE_FORMATTER);
            } else if (length >= DatePattern.NORM_DATETIME_MS_PATTERN.length() - 2) {
                return parseDateTime(dateStr, DatePattern.NORM_DATETIME_MS_FORMATTER);
            }
        } catch (Exception e) {
            throw new DateException("Parse [" + dateStr + "] with format normal error!");
        }

        // 没有更多匹配的时间格式
        throw new DateException("Parse [" + dateStr + "] format is not fit for date pattern!");
    }


    // ------------------------------------ Parse end ----------------------------------------------

    /**
     * 判断两个时间相差的时长
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return Duration
     */
    public static Duration duration(Temporal begin, Temporal end) {
        return Duration.between(begin, end);
    }

    public static Period period(LocalDate begin, LocalDate end) {
        return Period.between(begin, end);
    }


    /**
     * 判断两个时间相差的时长(秒)
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return 时间差
     */
    public static long betweenSeconds(LocalDateTime begin, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(begin, end);
    }

    /**
     * 判断两个时间相差的纳秒数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return 时间差
     */
    public static long betweenNanos(LocalDateTime begin, LocalDateTime end) {
        return ChronoUnit.NANOS.between(begin, end);
    }


    /**
     * 判断两个时间相差的微秒数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return 时间差
     */
    public static long betweenMicros(LocalDateTime begin, LocalDateTime end) {
        return ChronoUnit.MICROS.between(begin, end);
    }

    /**
     * 判断两个时间相差的毫秒数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return 时间差
     */
    public static long betweenMillis(LocalDateTime begin, LocalDateTime end) {
        return ChronoUnit.MILLIS.between(begin, end);
    }

    /**
     * 判断两个时间相差的分钟数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return 时间差
     */
    public static long betweenMinutes(LocalDateTime begin, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(begin, end);
    }

    /**
     * 判断两个时间相差的小时数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return 时间差
     */
    public static long betweenHours(LocalDateTime begin, LocalDateTime end) {
        return ChronoUnit.HOURS.between(begin, end);
    }

    /**
     * 判断两个时间相差的天数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return 时间差
     */
    public static long betweenDays(LocalDate begin, LocalDate end) {
        return ChronoUnit.DAYS.between(begin, end);
    }

    /**
     * 判断两个日期相差的周数
     *
     * @param begin 起始日期
     * @param end   结束日期
     * @return 日期差
     */
    public static long betweenWeeks(LocalDate begin, LocalDate end) {
        return ChronoUnit.WEEKS.between(begin, end);
    }

    /**
     * 判断两个日期相差的月数
     *
     * @param begin 起始日期
     * @param end   结束日期
     * @return 日期差
     */
    public static long betweenMonths(LocalDate begin, LocalDate end) {
        return ChronoUnit.MONTHS.between(begin, end);
    }

    /**
     * 判断两个日期相差的年数
     *
     * @param begin 起始日期
     * @param end   结束日期
     * @return 日期差
     */
    public static long betweenYears(LocalDate begin, LocalDate end) {
        return ChronoUnit.YEARS.between(begin, end);
    }

    /**
     * 获取毫秒数
     *
     * @param dateTime 日期
     * @return 日期的毫秒数
     */
    public static long getEpochMilli(LocalDateTime dateTime) {
        return toInstant(dateTime).toEpochMilli();
    }
    public static long getEpochMilli(OffsetDateTime offsetDateTime) {
        return toInstant(offsetDateTime).toEpochMilli();
    }
    /**
     * 获取秒数
     *
     * @param dateTime 日期
     * @return 日期的秒数
     */
    public static long getEpochSecond(LocalDateTime dateTime) {
        return toInstant(dateTime).getEpochSecond();
    }
    public static long getEpochSecond(OffsetDateTime offsetDateTime) {
        return toInstant(offsetDateTime).getEpochSecond();
    }

    /**
     * 是否闰年
     *
     * @param year 年
     * @return 是否闰年
     */
    public static boolean isLeapYear(int year) {
        return Year.of(year).isLeap();
    }

    /**
     * 判断datetime是否是符合format格式的日期时间字符串
     *
     * @param datetime
     * @param format
     * @return
     */
    public static boolean matchFormat(final String datetime, final String format) {
        try {
            return matchFormat(datetime, new SimpleDateFormat(format));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断datetime是否是符合dateFormat格式的日期时间字符串
     * 有些奇怪的格式，SimpleDateFormat才能判断出来。比如 yyyyMM,DateTimeFormatter无法判断
     *
     * @param datetime
     * @param dateFormat
     * @return
     */
    public static boolean matchFormat(final String datetime, final SimpleDateFormat dateFormat) {
        if (datetime == null || dateFormat == null || datetime.trim().length() == 0) {
            return false;
        }
        try {
            dateFormat.setLenient(false);
            Date date = dateFormat.parse(datetime);
            String result = dateFormat.format(date);
            return datetime.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断datetime是否是符合dateFormat格式的日期时间字符串
     *
     * @param datetime
     * @param dateFormat
     * @return
     */
    public static boolean matchFormat(final String datetime, final DateTimeFormatter dateFormat) {
        if (datetime == null || dateFormat == null || datetime.trim().length() == 0) {
            return false;
        }
        try {
            TemporalAccessor date = dateFormat.parse(datetime);
            String result = dateFormat.format(date);
            return datetime.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前日期所在月份的第一天
     *
     * @param date
     * @return
     */
    public static LocalDate getMonthFirstDate(LocalDate date) {
        return getMonthFirstDate(toYearMonth(date));
    }

    public static LocalDate getMonthFirstDate(YearMonth yearMonth) {
        return yearMonth.atDay(1);
    }

    /**
     * 获取当前日期所在月份的最后一天
     *
     * @param date
     * @return
     */
    public static LocalDate getMonthLastDate(LocalDate date) {
        return getMonthLastDate(toYearMonth(date));
    }

    /**
     * 获取当前年月所在月份的最后一天
     *
     * @param yearMonth
     * @return
     */
    public static LocalDate getMonthLastDate(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth();
    }


    /**
     * 获取当前日期时间所在月份的第一天，时间为00:00:00
     *
     * @param dateTime
     * @return
     */
    public static LocalDateTime getMonthFirstDateTime(LocalDateTime dateTime) {
        return getMonthFirstDateTime(toYearMonth(dateTime));
    }

    /**
     * 获取当前日期所在月份的第一天，时间为00:00:00
     *
     * @param date
     * @return
     */
    public static LocalDateTime getMonthFirstDateTime(LocalDate date) {
        return getMonthFirstDateTime(toYearMonth(date));
    }

    public static LocalDateTime getMonthFirstDateTime(YearMonth yearMonth) {
        return LocalDateTime.of(yearMonth.atDay(1), LocalTime.MIN);
    }

    /**
     * 获取当前日期时间所在月份的最后一天，时间为23:59:59.999_999_999
     *
     * @param dateTime
     * @return
     */
    public static LocalDateTime getMonthLastDateTime(LocalDateTime dateTime) {
        return getMonthLastDateTime(toYearMonth(dateTime));
    }

    /**
     * 获取当前日期所在月份的最后一天，时间为23:59:59.999_999_999
     *
     * @param date
     * @return
     */
    public static LocalDateTime getMonthLastDateTime(LocalDate date) {
        return getMonthLastDateTime(toYearMonth(date));
    }

    /**
     * 获取当前年月所在月份的最后一天，时间为23:59:59.999_999_999
     *
     * @param yearMonth
     * @return
     */
    public static LocalDateTime getMonthLastDateTime(YearMonth yearMonth) {
        return LocalDateTime.of(yearMonth.atEndOfMonth(), LocalTime.MAX);
    }

    /**
     * 获取指定日期的那一周的指定星期是哪一天
     * @param date
     * @param distWeek
     * @return
     */
    public static LocalDate getDateOfWeek(LocalDate date, DayOfWeek distWeek){
        if(date.getDayOfWeek()==distWeek){
            return date;
        }
        return date.with(distWeek);
    }

    /**
     * 获取当前日期的当周的指定星期是哪一天
     * @param distWeek
     * @return
     */
    public static LocalDate getDateOfWeek(DayOfWeek distWeek){
        LocalDate date=LocalDate.now();
        if(date.getDayOfWeek()==distWeek){
            return date;
        }
        return date.with(distWeek);
    }
    //======================日期时间比较============================================
    public static boolean isBeforeOrEqual(LocalDate start, LocalDate end) {
        return start.isBefore(end) || start.isEqual(end);
    }

    public static boolean isAfterOrEqual(LocalDate start, LocalDate end) {
        return start.isAfter(end) || start.isEqual(end);
    }

    public static boolean isBeforeOrEqual(LocalDateTime start, LocalDateTime end) {
        return start.isBefore(end) || start.isEqual(end);
    }
    public static boolean isBeforeOrEqual(OffsetDateTime start, OffsetDateTime end) {
        return start.isBefore(end) || start.isEqual(end);
    }
    public static boolean isAfterOrEqual(LocalDateTime start, LocalDateTime end) {
        return start.isAfter(end) || start.isEqual(end);
    }
    public static boolean isAfterOrEqual(OffsetDateTime start, OffsetDateTime end) {
        return start.isAfter(end) || start.isEqual(end);
    }
    public static boolean isBeforeOrEqual(LocalTime start, LocalTime end) {
        return start.isBefore(end) || start.equals(end);
    }

    public static boolean isAfterOrEqual(LocalTime start, LocalTime end) {
        return start.isAfter(end) || start.equals(end);
    }

    public static boolean isBeforeOrEqual(Date start, Date end) {
        return start.compareTo(end) <= 0;
    }

    public static boolean isAfterOrEqual(Date start, Date end) {
        return start.compareTo(end) >= 0;
    }

    public static boolean isBeforeOrEqual(Timestamp start, Timestamp end) {
        return start.compareTo(end) <= 0;
    }

    public static boolean isAfterOrEqual(Timestamp start, Timestamp end) {
        return start.compareTo(end) >= 0;
    }


    public static boolean isBefore(LocalDate start, LocalDate end) {
        return start.isBefore(end);
    }

    public static boolean isAfter(LocalDate start, LocalDate end) {
        return start.isAfter(end);
    }

    public static boolean isBefore(LocalDateTime start, LocalDateTime end) {
        return start.isBefore(end);
    }
    public static boolean isBefore(OffsetDateTime start, OffsetDateTime end) {
        return start.isBefore(end);
    }
    public static boolean isAfter(LocalDateTime start, LocalDateTime end) {
        return start.isAfter(end);
    }
    public static boolean isAfter(OffsetDateTime start, OffsetDateTime end) {
        return start.isAfter(end);
    }
    public static boolean isBefore(LocalTime start, LocalTime end) {
        return start.isBefore(end);
    }

    public static boolean isAfter(LocalTime start, LocalTime end) {
        return start.isAfter(end);
    }

    public static boolean isBefore(Date start, Date end) {
        return start.compareTo(end) < 0;
    }

    public static boolean isAfter(Date start, Date end) {
        return start.compareTo(end) > 0;
    }

    public static boolean isBefore(Timestamp start, Timestamp end) {
        return start.compareTo(end) < 0;
    }

    public static boolean isAfter(Timestamp start, Timestamp end) {
        return start.compareTo(end) > 0;
    }

    /**
     * 判断日期时间是否在一个区间内
     * @param now  要判断的日期时间
     * @param start 开始区间
     * @param end 结束区间
     * @param isIncludeStart  开始区间是否是闭区间，即包含开始值
     * @param isIncludeEnd  结束区间是否是闭区间，即包含结束值
     * @return
     */
    public static boolean isBetween(LocalDateTime now,LocalDateTime start, LocalDateTime end,boolean isIncludeStart, boolean isIncludeEnd){
        return (isIncludeStart?isAfterOrEqual(now,start):isAfter(now,start))
                && (isIncludeEnd?isBeforeOrEqual(now,end):isBefore(now,end));
    }
    /**
     * 判断日期时间是否在一个区间内
     * @param now  要判断的日期时间
     * @param start 开始区间
     * @param end 结束区间
     * @param isInclude    区间是否是闭区间，即包含开始与结束值
     * @return
     */
    public static boolean isBetween(LocalDateTime now,LocalDateTime start, LocalDateTime end,boolean isInclude){
        return (isInclude?isAfterOrEqual(now,start):isAfter(now,start))
                && (isInclude?isBeforeOrEqual(now,end):isBefore(now,end));
    }
    /**
     * 判断日期是否在一个区间内
     * @param now  要判断的日期
     * @param start 开始区间
     * @param end 结束区间
     * @param isIncludeStart  开始区间是否是闭区间，即包含开始值
     * @param isIncludeEnd  结束区间是否是闭区间，即包含结束值
     * @return
     */
    public static boolean isBetween(LocalDate now,LocalDate start, LocalDate end,boolean isIncludeStart, boolean isIncludeEnd){
        return (isIncludeStart?isAfterOrEqual(now,start):isAfter(now,start))
                && (isIncludeEnd?isBeforeOrEqual(now,end):isBefore(now,end));
    }
    /**
     * 判断日期是否在一个区间内
     * @param now  要判断的日期
     * @param start 开始区间
     * @param end 结束区间
     * @param isInclude    区间是否是闭区间，即包含开始与结束值
     * @return
     */
    public static boolean isBetween(LocalDate now,LocalDate start, LocalDate end,boolean isInclude){
        return (isInclude?isAfterOrEqual(now,start):isAfter(now,start))
                && (isInclude?isBeforeOrEqual(now,end):isBefore(now,end));
    }
    /**
     * 判断日期是否在一个区间内
     * @param now  要判断的日期
     * @param start 开始区间
     * @param end 结束区间
     * @param isIncludeStart  开始区间是否是闭区间，即包含开始值
     * @param isIncludeEnd  结束区间是否是闭区间，即包含结束值
     * @return
     */
    public static boolean isBetween(Date now,Date start, Date end,boolean isIncludeStart, boolean isIncludeEnd){
        return (isIncludeStart?isAfterOrEqual(now,start):isAfter(now,start))
                && (isIncludeEnd?isBeforeOrEqual(now,end):isBefore(now,end));
    }
    /**
     * 判断日期是否在一个区间内
     * @param now  要判断的日期
     * @param start 开始区间
     * @param end 结束区间
     * @param isInclude    区间是否是闭区间，即包含开始与结束值
     * @return
     */
    public static boolean isBetween(Date now,Date start, Date end,boolean isInclude){
        return (isInclude?isAfterOrEqual(now,start):isAfter(now,start))
                && (isInclude?isBeforeOrEqual(now,end):isBefore(now,end));
    }
    /**
     * 判断时间是否在一个区间内
     * @param now  要判断的时间
     * @param start 开始区间
     * @param end 结束区间
     * @param isIncludeStart  开始区间是否是闭区间，即包含开始值
     * @param isIncludeEnd  结束区间是否是闭区间，即包含结束值
     * @return
     */
    public static boolean isBetween(LocalTime now,LocalTime start, LocalTime end,boolean isIncludeStart, boolean isIncludeEnd){
        return (isIncludeStart?isAfterOrEqual(now,start):isAfter(now,start))
                && (isIncludeEnd?isBeforeOrEqual(now,end):isBefore(now,end));
    }
    /**
     * 判断时间是否在一个区间内
     * @param now  要判断的时间
     * @param start 开始区间
     * @param end 结束区间
     * @param isInclude    区间是否是闭区间，即包含开始与结束值
     * @return
     */
    public static boolean isBetween(LocalTime now,LocalTime start, LocalTime end,boolean isInclude){
        return (isInclude?isAfterOrEqual(now,start):isAfter(now,start))
                && (isInclude?isBeforeOrEqual(now,end):isBefore(now,end));
    }
    /**
     * 判断日期时间是否在一个闭区间内
     * @param now
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetween(LocalDateTime now,LocalDateTime start, LocalDateTime end){
        return isAfterOrEqual(now,start) && isBeforeOrEqual(now,end);
    }

    /**
     * 判断一个日期是否在闭区间内
     * @param now
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetween(LocalDate now,LocalDate start, LocalDate end){
        return isAfterOrEqual(now,start) && isBeforeOrEqual(now,end);
    }
    /**
     * 判断一个日期是否在闭区间内
     * @param now
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetween(Date now,Date start, Date end){
        return isAfterOrEqual(now,start) && isBeforeOrEqual(now,end);
    }
    /**
     * 判断一个时间是否在闭区间内
     * @param now
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetween(LocalTime now,LocalTime start, LocalTime end){
        return isAfterOrEqual(now,start) && isBeforeOrEqual(now,end);
    }


}
