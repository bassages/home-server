package nl.wiegman.home;

import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    public LocalDateTime getFromDateTime() {
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

    public boolean isWithinPeriod(LocalDateTime localDateTime) {
        return (localDateTime.isEqual(this.startDateTime) || localDateTime.isAfter(this.startDateTime)) && localDateTime.isBefore(this.toDateTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        DateTimePeriod that = (DateTimePeriod) o;

        return new EqualsBuilder().append(startDateTime, that.startDateTime).append(endDateTime, that.endDateTime).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(startDateTime).append(endDateTime).toHashCode();
    }

    @Override
    public String toString() {
        return "DateTimePeriod{" + "startDateTime=" + startDateTime + ", endDateTime=" + endDateTime + ", toDateTime=" + toDateTime + '}';
    }
}
