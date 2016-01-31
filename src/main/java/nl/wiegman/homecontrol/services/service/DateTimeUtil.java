package nl.wiegman.homecontrol.services.service;

import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

public class DateTimeUtil {

    public static List<Date> getDagenInPeriode(long van, long totEnMet) {
        List<Date> dagenInPeriode = new ArrayList<>();

        Date datumVan = DateUtils.truncate(new Date(van), Calendar.DATE);
        Date datumTotEnMet = DateUtils.truncate(new Date(totEnMet), Calendar.DATE);

        Date datum = datumVan;

        while (true) {
            dagenInPeriode.add(datum);

            if (DateUtils.isSameDay(datum, datumTotEnMet)) {
                break;
            } else {
                datum = DateUtils.addDays(datum, 1);
            }
        }
//        Collections.reverse(dagenInPeriode);
        return dagenInPeriode;
    }

    public static long getStartOfDay(Date day) {
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(day);

        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        return startOfDay.getTimeInMillis();
    }

    public static long getEndOfDay(Date day) {
        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(day);

        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 0);

        return endOfDay.getTimeInMillis();
    }
}
