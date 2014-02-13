package com.topsoft.search.solr.schedule;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;

/**
 * @author wangyg
 */
public class Suspension {
  private final List<TimeRange> timeRanges;
  private final List<MonthDay> monthDays;
  private final List<WeekdayOfWeekOfMonth> weekdayOfWeekOfMonths;
  private final List<Integer> weekdays;

  Suspension(List<TimeRange> timeRanges, List<MonthDay> monthDays, List<WeekdayOfWeekOfMonth> weekdayOfWeekOfMonths,
             List<Integer> weekdays) {
    this.timeRanges = timeRanges == null ? new ArrayList<TimeRange>() : ImmutableList.copyOf(timeRanges);
    this.monthDays = monthDays == null ? new ArrayList<MonthDay>() : ImmutableList.copyOf(monthDays);
    this.weekdayOfWeekOfMonths = weekdayOfWeekOfMonths == null ? new ArrayList<WeekdayOfWeekOfMonth>():
        ImmutableList.copyOf(weekdayOfWeekOfMonths);
    this.weekdays = weekdays == null ? new ArrayList<Integer>(): ImmutableList.copyOf(weekdays);
  }

  public static final Builder builder() {
    return new Builder();
  }

  public boolean shouldSuspend(Date date) {
    final DateTime dateTime = new DateTime(date);

    for (TimeRange timeRange : timeRanges) {
      if (timeRange.toDateTimeAndContains(dateTime)) {
        return true;
      }
    }

    for (MonthDay monthDay : monthDays) {
      if (dateTime.getDayOfMonth() == monthDay.getDayOfMonth() && dateTime.getMonthOfYear() == monthDay.getMonthOfYear()) {
        return true;
      }
    }

    for (WeekdayOfWeekOfMonth weekdayOfWeekOfMonth : weekdayOfWeekOfMonths) {
      if (weekdayOfWeekOfMonth.toDateAndEquals(dateTime)) {
        return true;
      }
    }

    for (Integer weekday : weekdays) {
      if (dateTime.getDayOfWeek() == weekday) {
        return true;
      }
    }

    return false;
  }

  public static class Builder {
    List<TimeRange> timeRanges = newLinkedList();
    List<MonthDay> monthDays = newLinkedList();
    List<WeekdayOfWeekOfMonth> weekdayOfWeekOfMonths = newLinkedList();
    List<Integer> weekdays = newLinkedList();

    private Builder() {
    }

    public Builder addTimeRange(LocalTime from, LocalTime to) {
      timeRanges.add(new TimeRange(checkNotNull(from), checkNotNull(to)));
      return this;
    }

    public Builder addMonthDay(int monthOfYear, int dayOfMonth) {
      checkArgument(monthOfYear > 0 && monthOfYear < 13, "monthOfYear must greater than zero and lesser than 13!");
      checkArgument(dayOfMonth > 0 && dayOfMonth < 32, "dayOfMonth must greater than zero and lesser than 32!");
      monthDays.add(new MonthDay(monthOfYear, dayOfMonth));
      return this;
    }

    public Builder addWeekDayOfWeekOfMonth(int dayOfWeek, int weekOfMonth, int monthOfYear) {
      checkArgument(dayOfWeek > 0 && dayOfWeek < 8, "dayOfMonth must greater than zero and lesser than 8!");
      checkArgument(weekOfMonth > 0 && weekOfMonth < 6, "dayOfMonth must greater than zero and lesser than 6!");
      checkArgument(monthOfYear > 0 && monthOfYear < 13, "monthOfYear must greater than zero and lesser than 13!");
      weekdayOfWeekOfMonths.add(new WeekdayOfWeekOfMonth(dayOfWeek, weekOfMonth, monthOfYear));
      return this;
    }

    public Builder addWeekDay(int dayOfWeek) {
      weekdays.add(dayOfWeek);

      return this;
    }

    public Suspension build() {
      return new Suspension(timeRanges, monthDays, weekdayOfWeekOfMonths, weekdays);
    }
  }

  public static class TimeRange {
    LocalTime from, to;

    TimeRange(LocalTime from, LocalTime to) {
      this.from = from;
      this.to = to;
    }

    public boolean toDateTimeAndContains(DateTime dateTime) {
      DateTime fromDt = from.toDateTime(dateTime);
      DateTime toDt = to.toDateTime(dateTime);

      // to.isBefore(from) if true means that from today to tomorrow
      // for example 3:00-2:00 means that from 3:00 of today to 2:00 of tomorrow
      if (toDt.isBefore(fromDt)) {
        toDt = toDt.plusDays(1);
      }

      // if from <= datetime < to, return true
      if ((dateTime.isEqual(fromDt) || dateTime.isAfter(fromDt)) && dateTime.isBefore(toDt)) {
        return true;
      }

      return false;
    }
  }

  public static class WeekdayOfWeekOfMonth {
    int dayOfWeek, weekOfMonth, month;

    WeekdayOfWeekOfMonth(int dayOfWeek, int weekOfMonth, int monthOfYear) {
      this.dayOfWeek = dayOfWeek;
      this.weekOfMonth = weekOfMonth;
      this.month = monthOfYear;
    }

    private LocalDate toLocalDate(int year) {
      LocalDate start = new LocalDate(year, month, 1);
      LocalDate date = start.withDayOfWeek(dayOfWeek);

      return (date.isBefore(start)) ? date.plusWeeks(weekOfMonth) : date.plusWeeks(weekOfMonth - 1);
    }

    public boolean toDateAndEquals(DateTime dateTime) {
      return toLocalDate(dateTime.getYear()).equals(dateTime.toLocalDate());
    }
  }

}
