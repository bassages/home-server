package nl.homeserver;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

import lombok.Data;

@Data
public class DateTimePeriod {

    private final LocalDateTime startDateTime;

    private final LocalDateTime endDateTime;
    private final LocalDateTime toDateTime;

    private DateTimePeriod(LocalDateTime startDateTime, LocalDateTime toDateTime) {
        this.startDateTime = startDateTime;
        this.toDateTime = toDateTime;
        this.endDateTime = toDateTime.minusNanos(1);
    }

    public LocalDateTime getFromDateTime() {
        return startDateTime;
    }

    public static DateTimePeriod aPeriodWithEndDateTime(LocalDateTime startDate, LocalDateTime endDateTime) {
        return new DateTimePeriod(startDate, endDateTime.plusNanos(1));
    }

    public static DateTimePeriod aPeriodWithToDateTime(LocalDateTime startDateTime, LocalDateTime toDateTime) {
        return new DateTimePeriod(startDateTime, toDateTime);
    }

    public boolean isWithinPeriod(LocalDateTime localDateTime) {
        return (localDateTime.isEqual(this.startDateTime) || localDateTime.isAfter(this.startDateTime)) && localDateTime.isBefore(this.toDateTime);
    }

    public List<LocalDate> getDays() {
        Validate.notNull(this.toDateTime, "DateTimePeriod must must be ending at some point of time");

        LocalDate from = startDateTime.toLocalDate();
        LocalDate to = toDateTime.toLocalDate();

        return Stream.iterate(from, date -> date.plusDays(1))
                     .limit(DAYS.between(from, to))
                     .collect(toList());
    }
}
