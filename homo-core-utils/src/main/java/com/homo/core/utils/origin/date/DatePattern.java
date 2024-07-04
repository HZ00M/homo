package com.homo.core.utils.origin.date;


import java.time.format.DateTimeFormatter;

/**
 * 日期格式化类，提供常用的日期格式化对象

 *
 */
public class DatePattern {

	/** 标准日期格式：yyyy-MM-dd */
	public final static String NORM_DATE_PATTERN = "yyyy-MM-dd";
	/** 标准日期格式 {@link DateTimeFormatter}：yyyy-MM-dd*/
	public final  static DateTimeFormatter NORM_DATE_FORMATTER = DateTimeFormatter.ofPattern(NORM_DATE_PATTERN);

	/** 标准日期格式：yyyy-M-d */
	public final static String NORM_DATE_PATTERN_SHORT = "yyyy-M-d";
	/** 标准日期格式 {@link DateTimeFormatter}：yyyy-M-d*/
	public final  static DateTimeFormatter NORM_DATE_FORMATTER_SHORT = DateTimeFormatter.ofPattern(NORM_DATE_PATTERN_SHORT);

	/** 标准时间格式：HH:mm:ss */
	public final static String NORM_TIME_PATTERN = "HH:mm:ss";
	/** 标准时间格式 {@link DateTimeFormatter}：HH:mm:ss */
	public final  static DateTimeFormatter NORM_TIME_FORMATTER = DateTimeFormatter.ofPattern(NORM_TIME_PATTERN);

	/** 标准时间格式：H:m:s */
	public final static String NORM_TIME_PATTERN_SHORT = "H:m:s";
	/** 标准时间格式 {@link DateTimeFormatter}：H:m:s */
	public final  static DateTimeFormatter NORM_TIME_FORMATTER_SHORT = DateTimeFormatter.ofPattern(NORM_TIME_PATTERN_SHORT);

	/** 斜线日期格式：
	 * yyyy/MM/dd
	 * yyyy/M/dd

	 * yyyy/M/d
	 *  */
	public final static String SLASH_DATE_PATTERN= "yyyy/MM/dd";
	public final static String SLASH_DATE_PATTERN_SHORT = "yyyy/M/d";
	/** excel日期格式 {@link DateTimeFormatter}：
	 * yyyy/MM/dd
	 * yyyy/M/d
	 * */
	public final  static DateTimeFormatter SLASH_DATE_FORMATTER = DateTimeFormatter.ofPattern(SLASH_DATE_PATTERN);
	public final  static DateTimeFormatter SLASH_DATE_FORMATTER_SHORT = DateTimeFormatter.ofPattern(SLASH_DATE_PATTERN_SHORT);


	/** 标准时间格式：HH:mm */
	public final static String NORM_MIN_TIME_PATTERN = "HH:mm";
	/** 标准时间格式 {@link DateTimeFormatter}：HH:mm*/
	public final  static DateTimeFormatter  NORM_MIN_TIME_FORMATTER = DateTimeFormatter.ofPattern(NORM_MIN_TIME_PATTERN);

	/** 标准日期时间格式，精确到分：yyyy-MM-dd HH:mm */
	public final static String NORM_DATETIME_MINUTE_PATTERN = "yyyy-MM-dd HH:mm";
	/** 标准日期时间格式，精确到分 {@link DateTimeFormatter}：yyyy-MM-dd HH:mm */
	public final  static DateTimeFormatter NORM_DATETIME_MINUTE_FORMATTER = DateTimeFormatter.ofPattern(NORM_DATETIME_MINUTE_PATTERN);
	/** 标准日期时间格式，精确到秒：yyyy-MM-dd HH:mm:ss */
	public final static String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	/** 标准日期时间格式，精确到秒 {@link DateTimeFormatter}：yyyy-MM-dd HH:mm:ss */
	public final  static DateTimeFormatter NORM_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(NORM_DATETIME_PATTERN);
	/** 标准日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss.SSS */
	public final static String NORM_DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
	/** 标准日期时间格式，精确到毫秒 {@link DateTimeFormatter}：yyyy-MM-dd HH:mm:ss.SSS */
	public final  static DateTimeFormatter NORM_DATETIME_MS_FORMATTER = DateTimeFormatter.ofPattern(NORM_DATETIME_MS_PATTERN);

	/** 标准年月格式：yyyy-MM */
	public final static String NORM_YEAR_MONTH_PATTERN = "yyyy-MM";
	/** 标准年月格式 {@link DateTimeFormatter}：yyyy-MM **/
	public final  static DateTimeFormatter NORM_YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern(NORM_YEAR_MONTH_PATTERN);
	/** 标准月日格式：MM-dd */
	public final static String NORM_MONTH_DAY_PATTERN = "MM-dd";
	/** 标准月日格式 {@link DateTimeFormatter}：MM-dd **/
	public final  static DateTimeFormatter NORM_MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern(NORM_MONTH_DAY_PATTERN);

	/** 整数日期格式：yyyyMMdd */
	public final static String INT_DATE_PATTERN = "yyyyMMdd";
	/** 整数日期格式 {@link DateTimeFormatter}：yyyyMMdd*/
	public final  static DateTimeFormatter INT_DATE_FORMATTER = DateTimeFormatter.ofPattern(INT_DATE_PATTERN);

	/** 整数日期时间格式：yyyyMMddHHmmss */
	public final static String LONG_DATETIME_PATTERN = "yyyyMMddHHmmss";
	/** 整数日期时间格式 {@link DateTimeFormatter}：yyyyMMddHHmmss*/
	public final  static DateTimeFormatter LONG_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(LONG_DATETIME_PATTERN);
	/** 整数年月：yyyyMM */
	public final static String INT_YEAR_MONTH_PATTERN = "yyyyMM";
	/** 整数年月 {@link DateTimeFormatter}：yyyyMM */
	public final  static DateTimeFormatter INT_YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern(INT_YEAR_MONTH_PATTERN);
	/** 整数时分秒 HHmmss**/
	public final static String INT_TIME_PATTERN = "HHmmss";
	/** 整数年月 {@link DateTimeFormatter}：HHmmss */
	public final  static DateTimeFormatter INT_TIME_FORMATTER = DateTimeFormatter.ofPattern(INT_TIME_PATTERN);
	/** 整数月日格式：MMdd */
	public final static String INT_MONTH_DAY_PATTERN = "MMdd";
	/** 整数月日格式 {@link DateTimeFormatter}：MMdd **/
	public final  static DateTimeFormatter INT_MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern(INT_MONTH_DAY_PATTERN);

	/** 整数日期格式：yyMMdd */
	public final static String INT_SHORT_DATE_PATTERN = "yyMMdd";
	/** 整数日期格式 {@link DateTimeFormatter}：yyMMdd*/
	public final  static DateTimeFormatter INT_SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern(INT_SHORT_DATE_PATTERN);

	/** 整数分钟日期格式：yyyyMMddHHmm */
	public final static String LONG_SHORT_DATE_MINUTE_PATTERN = "yyyyMMddHHmm";
	/** 整数分钟日期格式 {@link DateTimeFormatter}：yyyyMMddHHmm*/
	public final  static DateTimeFormatter LONG_SHORT_DATE_MINUTE_FORMATTER = DateTimeFormatter.ofPattern(LONG_SHORT_DATE_MINUTE_PATTERN);


	/**整数分钟日期格式： yyMMddHHmm */
	public final static String INT_LEES_SHORT_DATE_MINUTE_PATTERN = "yyMMddHHmm";
	/** 整数分钟日期格式 {@link DateTimeFormatter}：yyyyMMddHHmm*/
	public final  static DateTimeFormatter INT_LEES_SHORT_DATE_MINUTE_FORMATTER = DateTimeFormatter.ofPattern(INT_LEES_SHORT_DATE_MINUTE_PATTERN);
}
