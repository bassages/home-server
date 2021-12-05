package nl.homeserver;

import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;

import static java.time.Month.JANUARY;
import static java.time.temporal.ChronoUnit.DAYS;

@Data
@ToString
public class DateTimePeriod {

    private final LocalDateTime startDateTime;

    @ToString.Exclude
    private final LocalDateTime endDateTime;
    private final LocalDateTime toDateTime;

    public static DateTimePeriod of(final LocalDate day) {
        return aPeriodWithToDateTime(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
    }

    public static DateTimePeriod of(final YearMonth yearMonth) {
        final LocalDateTime from = yearMonth.atDay(1).atStartOfDay();
        final LocalDateTime to = from.plusMonths(1);
        return aPeriodWithToDateTime(from, to);
    }

    public static DateTimePeriod of(final Year year) {
        final LocalDateTime from = LocalDate.of(year.getValue(), JANUARY, 1).atStartOfDay();
        final LocalDateTime to = from.plusYears(1);
        return aPeriodWithToDateTime(from, to);
    }

    private DateTimePeriod(final LocalDateTime startDateTime, final LocalDateTime toDateTime) {
        this.startDateTime = startDateTime;
        this.toDateTime = toDateTime;
        this.endDateTime = toDateTime.minusNanos(1);
    }

    public LocalDateTime getFromDateTime() {
        return startDateTime;
    }

    public static DateTimePeriod aPeriodWithEndDateTime(final LocalDateTime startDateTime, final LocalDateTime endDateTime) {
        return new DateTimePeriod(startDateTime, endDateTime.plusNanos(1));
    }

    public static DateTimePeriod aPeriodWithToDateTime(final LocalDateTime fromDateTime, final LocalDateTime toDateTime) {
        return new DateTimePeriod(fromDateTime, toDateTime);
    }

    public boolean isWithinPeriod(final LocalDateTime localDateTime) {
        return (localDateTime.isEqual(this.startDateTime) || localDateTime.isAfter(this.startDateTime)) && localDateTime.isBefore(this.toDateTime);
    }

    public boolean startsOnOrAfter(final LocalDateTime dateTime) {
        return startDateTime.isEqual(dateTime) || startDateTime.isAfter(dateTime);
    }

    public List<LocalDate> getDays() {
        Validate.notNull(this.toDateTime, "DateTimePeriod must must be ending at some point of time");

        final LocalDate from = startDateTime.toLocalDate();
        final LocalDate to = toDateTime.toLocalDate();

        return Stream.iterate(from, date -> date.plusDays(1))
                     .limit(DAYS.between(from, to))
                     .toList();
    }
}
