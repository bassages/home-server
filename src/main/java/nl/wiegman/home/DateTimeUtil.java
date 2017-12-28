package nl.wiegman.home;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

public class DateTimeUtil {

    public static List<LocalDate> getDaysInPeriod(DateTimePeriod period) {
        Validate.notNull(period.getEndDateTime(), "DateTimePeriod must must be ending at some point of time");

        LocalDate datumVan = period.getStartDateTime().toLocalDate();
        LocalDate datumTot = period.getEndDateTime().toLocalDate().plusDays(1);

        return Stream.iterate(datumVan, date -> date.plusDays(1))
                .limit(DAYS.between(datumVan, datumTot))
                .collect(toList());
    }

    public static List<LocalDate> getDaysInPeriod(DatePeriod period) {
        return getDaysInPeriod(period.toDateTimePeriod());
    }

    public static Set<Month> getAllMonths() {
        return EnumSet.allOf(Month.class);
    }

    public static Date getStartOfDayAsDate(Date day) {
        Calendar startOfDay = getStartOfDayAsCalendar(day);
        return startOfDay.getTime();
    }

    private static Calendar getStartOfDayAsCalendar(Date day) {
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(day);
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        return startOfDay;
    }

    public static Date toDateAtStartOfDay(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static long toMillisSinceEpochAtStartOfDay(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    public static long toMillisSinceEpoch(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime toLocalDateTime(long millisSinceEpoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millisSinceEpoch), ZoneId.systemDefault());
    }

    public static LocalDate toLocalDate(long millisSinceEpoch) {
        return toLocalDateTime(millisSinceEpoch).toLocalDate();
    }
}
