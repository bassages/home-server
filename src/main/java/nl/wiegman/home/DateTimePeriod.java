package nl.wiegman.home;

import java.time.LocalDateTime;

public class DateTimePeriod {

    private final LocalDateTime startDateTime;

    private final LocalDateTime endDateTime;
    private final LocalDateTime toDateTime;

    private DateTimePeriod(LocalDateTime startDateTime, LocalDateTime toDateTime) {
        this.startDateTime = startDateTime;
        this.toDateTime = toDateTime;
        this.endDateTime = toDateTime.minusNanos(1);
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getToDateTime() {
        return toDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public static DateTimePeriod aPeriodWithEndDateTime(LocalDateTime startDate, LocalDateTime endDateTime) {
        return new DateTimePeriod(startDate, endDateTime.plusNanos(1));
    }

    public static DateTimePeriod aPeriodWithToDateTime(LocalDateTime startDateTime, LocalDateTime toDateTime) {
        return new DateTimePeriod(startDateTime, toDateTime);
    }
}
