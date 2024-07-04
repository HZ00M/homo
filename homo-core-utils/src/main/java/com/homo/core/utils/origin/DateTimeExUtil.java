package com.homo.core.utils.origin;


import com.homo.core.utils.origin.date.DateAndTimeRange;
import com.homo.core.utils.origin.date.DatePattern;
import com.homo.core.utils.origin.tuple.Tuple2;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 时间工具类
 * 
 *
 */
public  final  class DateTimeExUtil {

	private DateTimeExUtil() {
	}

	/**
	 * 按每5分钟分组时间
	 * @param dateTime
	 * @return
	 */
	public static LocalDateTime roundDateTimeBy5Minute(LocalDateTime dateTime){
		int minute=dateTime.getMinute();
		minute=minute- minute % 5;
		dateTime=dateTime.withMinute(minute).withSecond(0).withNano(0);
		return dateTime;
	}
	public static long timestampToIntTimeRoundMinute(@NotNull LocalDateTime dateTime) {
		return Long.parseLong(DatePattern.LONG_SHORT_DATE_MINUTE_FORMATTER.format(dateTime));
	}
	public static long timestampToIntTimeRoundMinute(@NotNull Instant instant) {
		return Long.parseLong(DatePattern.LONG_SHORT_DATE_MINUTE_FORMATTER.format(DateTimeUtil.toLocalDateTime(instant)));
	}
	public static long timestampToIntTimeRoundMinute(long timestamp) {
		return timestampToIntTimeRoundMinute(DateTimeUtil.toLocalDateTime(timestamp));
	}
	public static long  timestampToIntTimeRound5Minute(long timestamp){
		long minute=timestampToIntTimeRoundMinute(timestamp);
		long temp=minute % 10;
		minute=minute/10 *10 + (temp<5?0:5);
		return minute;
	}


	/**
	 * Get between begin and end all date
	 */
	public static List<Date> findDates(Date begin, Date end) {
		List dates = new ArrayList();
		dates.add(begin);
		Calendar calBegin = Calendar.getInstance();
		calBegin.setTime(begin);
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTime(end);
		while (end.after(calBegin.getTime())) {
			calBegin.add(Calendar.DAY_OF_MONTH, 1);
			dates.add(calBegin.getTime());
		}
		return dates;
	}

	public static List<LocalDate> findDates(LocalDate begin, LocalDate end){
		List<LocalDate> result = new ArrayList<>();
		val distance = ChronoUnit.DAYS.between(begin, end);
		if(distance < 1) {
			return result;
		}
		return Stream.iterate(begin, d -> d.plusDays(1)).limit(distance + 1).collect(Collectors.toList());
	}
	/**
	 * 判断今天是否是生日
	 *
	 * @param birthDay 生日，标准日期字符串
	 * @return 真与假
	 */
	public static boolean isBirthDay(LocalDate birthDay) {
		MonthDay monthDay=MonthDay.from(birthDay);
		return monthDay.equals(MonthDay.now());
	}
	/**
	 * 生日转为年龄，计算法定年龄
	 *
	 * @param birthDay 生日，标准日期字符串
	 * @return 年龄
	 */
	public static int ageOfNow(String birthDay) {
		return ageOfNow(DateTimeUtil.parseDate(birthDay));
	}

	/**
	 * 生日转为年龄，计算法定年龄
	 *
	 * @param birthDay 生日
	 * @return 年龄
	 */
	public static int ageOfNow(LocalDate birthDay) {
		return age(birthDay, LocalDate.now());
	}

	/**
	 * 计算年龄
	 *
	 * @param birthDay 生日
	 * @param dateToCompare 需要对比的日期
	 * @return 年龄
	 */
	public static int age(LocalDate birthDay,LocalDate dateToCompare) {


		int year = dateToCompare.getYear();
		int month = dateToCompare.getMonthValue();
		int dayOfMonth = dateToCompare.getDayOfMonth();

		int birthYear = birthDay.getYear();
		int birthMonth = birthDay.getMonthValue();
		int birthDayofMonth = birthDay.getDayOfMonth();



		int age = year - birthYear;

		if (month == birthMonth) {

			if (dayOfMonth < birthDayofMonth) {
				// 如果生日在当月，但是未达到生日当天的日期，年龄减一
				age--;
			}
		} else if (month < birthMonth) {
			// 如果当前月份未达到生日的月份，年龄计算减一
			age--;
		}
		return age;
	}


	/**
	 * 按小时拆分时间段。开始与结束时间都是闭区间。
	 * 时间精度到秒。如果时间精度是毫秒，则不适用
	 * @param start
	 * @param end
	 * @param intervalHour
	 * @return
	 */
	public static List<Tuple2<LocalDateTime,LocalDateTime>> splitHourByClosed(LocalDateTime start, LocalDateTime end, byte intervalHour){
		Assert.notNull(start,"开始时间不能为Null");
		Assert.notNull(end,"结束时间不能为Null");
		Assert.isTrue(intervalHour>0,"间隔小时数不能小于等于0");
		List<Tuple2<LocalDateTime,LocalDateTime>> list=new ArrayList<>();
		if(start.isEqual(end)){
			list.add(new Tuple2(start,end));
			return list;
		}
		LocalDateTime tmpStart=start;
		while(end.isAfter(tmpStart)){
			LocalDateTime tmpEnd=tmpStart.withMinute(59).withSecond(59).withNano(0);
			if(tmpEnd.isEqual(end) || tmpEnd.isAfter(end)){
				tmpEnd=end;
			}
			list.add(new Tuple2(tmpStart,tmpEnd));
			tmpStart=tmpStart.plusHours(intervalHour).withMinute(0).withSecond(0).withNano(0);
		}
		return list;
	}
	public static List<Tuple2<LocalDateTime,LocalDateTime>> splitHourByClosed(LocalDateTime start, LocalDateTime end){
		return splitHourByClosed(start,end,(byte)1);
	}
	/**
	 * 按小时拆分时间段。开始时间是闭区间。结束时间是开区间
	 * 时间精度到秒。但是由于结束时间为开区间，适用于秒的情况
	 * @param start
	 * @param end
	 * @param intervalHour
	 * @return
	 */
	public static List<Tuple2<LocalDateTime,LocalDateTime>> splitHourByOpen(LocalDateTime start,LocalDateTime end,byte intervalHour){
		Assert.notNull(start,"开始时间不能为Null");
		Assert.notNull(end,"结束时间不能为Null");
		Assert.isFalse(start.isEqual(end),"结束时间为开区间，开始时间不允许与结束时间相等");
		Assert.isTrue(intervalHour>0,"间隔小时数不能小于等于0");
		List<Tuple2<LocalDateTime,LocalDateTime>> list=new ArrayList<>();
		LocalDateTime tmpStart=start;
		while(end.isAfter(tmpStart)){
			LocalDateTime tmpEnd=tmpStart.plusHours(intervalHour).withMinute(0).withSecond(0).withNano(0);
			if(tmpEnd.isEqual(end) || tmpEnd.isAfter(end)){
				tmpEnd=end;
			}
			list.add(new Tuple2(tmpStart,tmpEnd));
			tmpStart=tmpEnd;
		}
		return list;
	}
	public static List<Tuple2<LocalDateTime,LocalDateTime>> splitHourByOpen(LocalDateTime start,LocalDateTime end){
		return splitHourByOpen(start,end,(byte)1);
	}


	public static List<LocalDate> datesBetween(LocalDate start, LocalDate endInclusive) {
		val dates = new ArrayList<LocalDate>();
		while (!start.isAfter(endInclusive)) {
			dates.add(start);
			start = start.plusDays(1);
		}
		return dates;
	}
	public static List<Date> datesBetween(Date start, Date endInclusive) {
		val dates = new ArrayList<Date>();
		while (!start.after(endInclusive)) {
			dates.add(start);
			start = DateTimeUtil.toDate(DateTimeUtil.toLocalDate(start).plusDays(1));
		}
		return dates;
	}
	public static List<Integer> datesBetween(Integer start, Integer endInclusive) {
		val dates = new ArrayList<Integer>();
		LocalDate startDate = DateTimeUtil.parseIntDate(start);
		LocalDate endDate = DateTimeUtil.parseIntDate(endInclusive);
		while (!startDate.isAfter(endDate)) {
			dates.add(DateTimeUtil.formatDateToInt(startDate));
			startDate = startDate.plusDays(1);
		}
		return dates;
	}
	public static List<YearMonth> monthsBetween(YearMonth start, YearMonth endInclusive) {
		val months = new ArrayList<YearMonth>();
		while (!start.isAfter(endInclusive)) {
			months.add(start);
			start = start.plusMonths(1);
		}
		return months;
	}


	/**
	 * 得到start与end之间的所有日期的，每一天的时间范围，开始时间为start的localTime，其nano设置为0。结束时间为end的localTime，其nano设置为999999999
	 * @param start
	 * @param end
	 * @return
	 */
	public static List<DateAndTimeRange> dateAndTimeRanges(LocalDateTime start, LocalDateTime end) {
		return dateAndTimeRanges(start,end,true);
	}
	/**
	 * 得到start与end之间的所有日期的，每一天的时间范围，开始时间为start的localTime，结束时间为end的localTime
	 * @param start
	 * @param end
	 * @param  transStartEndNanaTime 是否处理时间的nano段。false不处理，true处理，即设置开始时间的nano为0， 结束时间的nana为999999999
	 * @return
	 */
	public static List<DateAndTimeRange> dateAndTimeRanges(LocalDateTime start, LocalDateTime end,boolean transStartEndNanaTime) {
		LocalTime startTime=transStartEndNanaTime? start.toLocalTime().withNano(LocalTime.MIN.getNano()):start.toLocalTime();
		LocalTime endTime=transStartEndNanaTime? end.toLocalTime().withNano(LocalTime.MAX.getNano()):end.toLocalTime();
		List<DateAndTimeRange> rs = new ArrayList<>();
		while (!start.isAfter(end)) {
			rs.add(new DateAndTimeRange(start.toLocalDate(),startTime,endTime));
			start = start.plusDays(1);
		}
		return rs;
	}

	/**
	 * 得到startDate与endDate之间的所有日期的，每一天的时间范围，开始时间为start的localTime，结束时间为end的localTime
	 * @param startDate
	 * @param endDate
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static List<DateAndTimeRange> dateAndTimeRanges(LocalDate startDate, LocalDate endDate,LocalTime startTime, LocalTime endTime) {
		return dateAndTimeRanges(startDate,endDate,startTime,endTime,true);
	}
	/**
	 * 得到startDate与endDate之间的所有日期的，每一天的时间范围，开始时间为start的localTime，结束时间为end的localTime
	 * @param startDate
	 * @param endDate
	 * @param startTime
	 * @param endTime
	 * @param  transStartEndNanaTime 是否处理时间的nano段。false不处理，true处理，即设置开始时间的nano为0， 结束时间的nana为999999999
	 * @return
	 */
	public static List<DateAndTimeRange> dateAndTimeRanges(LocalDate startDate, LocalDate endDate,LocalTime startTime, LocalTime endTime,boolean transStartEndNanaTime) {
		if(transStartEndNanaTime){
			startTime=startTime.withNano(LocalTime.MIN.getNano());
			endTime=endTime.withNano(LocalTime.MAX.getNano());
		}
		List<DateAndTimeRange> rs = new ArrayList<>();
		while (!startDate.isAfter(endDate)) {
			rs.add(new DateAndTimeRange(startDate,startTime,endTime));
			startDate = startDate.plusDays(1);
		}
		return rs;
	}
}
