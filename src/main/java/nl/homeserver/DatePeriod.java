package nl.homeserver;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

public final class DatePeriod {

    private static final Set<DayOfWeek> WEEKENDDAYS = EnumSet.of(SATURDAY, SUNDAY);

    private final DateTimePeriod dateTimePeriod;

    public static DatePeriod of(final LocalDate day) {
        return aPeriodWithToDate(day, day.plusDays(1));
    }

    private DatePeriod(final DateTimePeriod dateTimePeriod) {
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

    public static DatePeriod aPeriodWithEndDate(final LocalDate startDate, final LocalDate endDate) {
        return new DatePeriod(DateTimePeriod.aPeriodWithEndDateTime(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay().minusNanos(1)));
    }

    public static DatePeriod aPeriodWithToDate(final LocalDate fromDate, final LocalDate toDate) {
        return new DatePeriod(DateTimePeriod.aPeriodWithToDateTime(fromDate.atStartOfDay(), toDate.atStartOfDay()));
    }

    public DateTimePeriod toDateTimePeriod() {
        return dateTimePeriod;
    }

    public List<LocalDate> getDays() {
        return dateTimePeriod.getDays();
    }

    public long getNumberOfWeekendDays() {
        return getDays().stream()
                        .filter(date -> WEEKENDDAYS.contains(date.getDayOfWeek()))
                        .count();
    }

    public long getNumberOfWeekDays() {
        return getDays().stream()
                        .filter(date -> !WEEKENDDAYS.contains(date.getDayOfWeek()))
                        .count();
    }

    public Stream<Integer> streamYears() {
        return IntStream.rangeClosed(getFromDate().getYear(), getToDate().getYear()).boxed();
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("dateTimePeriod", dateTimePeriod)
                .toString();
    }
}
