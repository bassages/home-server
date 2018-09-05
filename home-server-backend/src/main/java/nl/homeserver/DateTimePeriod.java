package nl.homeserver;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Data;

@Data
public class DateTimePeriod {

    private final LocalDateTime startDateTime;

    private final LocalDateTime endDateTime;
    private final LocalDateTime toDateTime;

    private DateTimePeriod(final LocalDateTime startDateTime, final LocalDateTime toDateTime) {
        this.startDateTime = startDateTime;
        this.toDateTime = toDateTime;
        this.endDateTime = toDateTime.minusNanos(1);
    }

    public LocalDateTime getFromDateTime() {
        return startDateTime;
    }

    public static DateTimePeriod aPeriodWithEndDateTime(final LocalDateTime startDate, final LocalDateTime endDateTime) {
        return new DateTimePeriod(startDate, endDateTime.plusNanos(1));
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
                     .collect(toList());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("startDateTime", startDateTime)
                .append("endDateTime", endDateTime)
                .append("toDateTime", toDateTime)
                .toString();
    }
}
