package nl.wiegman.home;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

public class DateTimeUtil {

    public static List<Date> getDagenInPeriode(long van, long totEnMet) {
        List<Date> dagenInPeriode = new ArrayList<>();

        Date datumVan = DateUtils.truncate(new Date(van), Calendar.DATE);
        Date datumTotEnMet = DateUtils.truncate(new Date(totEnMet), Calendar.DATE);

        if (datumVan.after(datumTotEnMet)) {
            throw new RuntimeException("van must be smaller or equal to totEnMet");
        }

        Date datum = datumVan;

        while (true) {
            dagenInPeriode.add(datum);

            if (DateUtils.isSameDay(datum, datumTotEnMet)) {
                break;
            } else {
                datum = DateUtils.addDays(datum, 1);
            }
        }
        return dagenInPeriode;
    }

    public static long getStartOfDay(Date day) {
        Calendar startOfDay = getStartOfDayAsCalendar(day);

        return startOfDay.getTimeInMillis();
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

    public static long getEndOfDay(Date day) {
        Calendar endOfDay = getEndOfDayAsCalendar(day);
        return endOfDay.getTimeInMillis();
    }

    private static Calendar getEndOfDayAsCalendar(Date day) {
        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(day);
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 0);
        return endOfDay;
    }

    public static Date getEndOfDayAsDate(Date day) {
        Calendar startOfDay = getStartOfDayAsCalendar(day);
        return startOfDay.getTime();
    }


    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static boolean isAfterToday(Date dag) {
        return getStartOfDay(dag) > getEndOfDay(new Date());
    }
}
