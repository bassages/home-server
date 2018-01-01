package nl.wiegman.home;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Test;

public class DateTimePeriodTest {

    @Test
    public void whenCreateDateTimePeriodWithEndDateTimeThenToDateTimeIsSet() {
        LocalDateTime startDateTime = LocalDateTime.of(2017, Month.SEPTEMBER, 12, 13, 57);
        LocalDateTime endDateTime = LocalDateTime.of(2017, Month.DECEMBER, 1, 5, 7);

        DateTimePeriod dateTimePeriod = DateTimePeriod.aPeriodWithEndDateTime(startDateTime, endDateTime);

        assertThat(dateTimePeriod.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dateTimePeriod.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(dateTimePeriod.getToDateTime()).isEqualTo(endDateTime.plusNanos(1));
    }

    @Test
    public void whenCreateDateTimePeriodWithToDateTimeThenEndDateTimeIsSet() {
        LocalDateTime startDateTime = LocalDateTime.of(2017, Month.SEPTEMBER, 12, 13, 57);
        LocalDateTime toDateTime = LocalDateTime.of(2017, Month.DECEMBER, 1, 5, 7);

        DateTimePeriod dateTimePeriod = DateTimePeriod.aPeriodWithToDateTime(startDateTime, toDateTime);

        assertThat(dateTimePeriod.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dateTimePeriod.getToDateTime()).isEqualTo(toDateTime);
        assertThat(dateTimePeriod.getEndDateTime()).isEqualTo(toDateTime.minusNanos(1));
    }

    @Test
    public void whenCreateDateTimePeriodThenStartDateTimeIsSameAsFromDateTime() {
        LocalDateTime startDateTime = LocalDateTime.of(2017, Month.SEPTEMBER, 12, 13, 57);

        DateTimePeriod dateTimePeriod = DateTimePeriod.aPeriodWithEndDateTime(startDateTime, startDateTime);

        assertThat(dateTimePeriod.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dateTimePeriod.getFromDateTime()).isEqualTo(startDateTime);
    }
}