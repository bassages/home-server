package nl.wiegman.home;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.Validate;

public class DateTimePeriod {

    private final LocalDateTime startDateTime;

    private final LocalDateTime endDateTime;
    private final LocalDateTime toDateTime;

    private DateTimePeriod(@NotNull LocalDateTime startDateTime, LocalDateTime toDateTime) {
        this.startDateTime = startDateTime;
        this.toDateTime = toDateTime;

        if (toDateTime == null) {
            this.endDateTime = null;
        } else {
            this.endDateTime = toDateTime.minusNanos(1);
        }
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

    public static DateTimePeriod aPeriodWhichNeverEnds(LocalDateTime startDate) {
        return new DateTimePeriod(startDate, null);
    }

    public static DateTimePeriod aPeriodWithEndDateTime(LocalDateTime startDate, LocalDateTime endDate) {
        return new DateTimePeriod(startDate, endDate.plusNanos(1));
    }

    public static DateTimePeriod aPeriodWithToDateTime(LocalDateTime startDate, LocalDateTime toDate) {
        return new DateTimePeriod(startDate, toDate);
    }
}
