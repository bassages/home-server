package nl.wiegman.home;

import java.time.LocalDate;

public class DatePeriod {

    private final DateTimePeriod dateTimePeriod;

    public DatePeriod(DateTimePeriod dateTimePeriod) {
        this.dateTimePeriod = dateTimePeriod;
    }

    public LocalDate getStartDate() {
        return dateTimePeriod.getStartDateTime().toLocalDate();
    }

    public LocalDate getFromDate() {
        return dateTimePeriod.getStartDateTime().toLocalDate();
    }

    public LocalDate getToDate() {
        return dateTimePeriod.getToDateTime().toLocalDate();
    }

    public LocalDate getEndDate() {
        return dateTimePeriod.getEndDateTime().toLocalDate();
    }

    public static DatePeriod aPeriodWithEndDate(LocalDate startDate, LocalDate endDate) {
        return new DatePeriod(DateTimePeriod.aPeriodWithEndDateTime(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay().minusNanos(1)));
    }

    public static DatePeriod aPeriodWithToDate(LocalDate startDate, LocalDate toDate) {
        return new DatePeriod(DateTimePeriod.aPeriodWithToDateTime(startDate.atStartOfDay(), toDate.atStartOfDay()));
    }

    public DateTimePeriod toDateTimePeriod() {
        return dateTimePeriod;
    }
}
