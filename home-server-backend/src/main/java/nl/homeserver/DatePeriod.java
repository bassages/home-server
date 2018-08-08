package nl.homeserver;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDate;
import java.util.List;

public final class DatePeriod {

    private final DateTimePeriod dateTimePeriod;

    public DatePeriod(final DateTimePeriod dateTimePeriod) {
        this.dateTimePeriod = dateTimePeriod;
    }

    public LocalDate getFromDate() {
        return dateTimePeriod.getFromDateTime().toLocalDate();
    }

    public LocalDate getToDate() {
        return dateTimePeriod.getToDateTime().toLocalDate();
    }

    public LocalDate getEndDate() {
        return dateTimePeriod.getEndDateTime().toLocalDate();
    }

    public static DatePeriod aPeriodWithEndDate(final LocalDate startDate, LocalDate endDate) {
        return new DatePeriod(DateTimePeriod.aPeriodWithEndDateTime(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay().minusNanos(1)));
    }

    public static DatePeriod aPeriodWithToDate(final LocalDate startDate, final LocalDate toDate) {
        return new DatePeriod(DateTimePeriod.aPeriodWithToDateTime(startDate.atStartOfDay(), toDate.atStartOfDay()));
    }

    public DateTimePeriod toDateTimePeriod() {
        return dateTimePeriod;
    }

    public List<LocalDate> getDays() {
        return dateTimePeriod.getDays();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DatePeriod period = (DatePeriod) o;

        return new EqualsBuilder().append(dateTimePeriod, period.dateTimePeriod).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(dateTimePeriod).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("dateTimePeriod", dateTimePeriod)
                .toString();
    }
}
