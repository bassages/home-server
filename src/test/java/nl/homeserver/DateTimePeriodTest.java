package nl.homeserver;

import static java.time.Month.JANUARY;
import static nl.homeserver.DateTimePeriod.aPeriodWithEndDateTime;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import org.junit.Test;

public class DateTimePeriodTest {

    @Test
    public void whenCreateDateTimePeriodWithEndDateTimeThenToDateTimeIsSet() {
        LocalDateTime startDateTime = LocalDateTime.of(2017, Month.SEPTEMBER, 12, 13, 57);
        LocalDateTime endDateTime = LocalDateTime.of(2017, Month.DECEMBER, 1, 5, 7);

        DateTimePeriod dateTimePeriod = aPeriodWithEndDateTime(startDateTime, endDateTime);

        assertThat(dateTimePeriod.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dateTimePeriod.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(dateTimePeriod.getToDateTime()).isEqualTo(endDateTime.plusNanos(1));
    }

    @Test
    public void whenCreateDateTimePeriodWithToDateTimeThenEndDateTimeIsSet() {
        LocalDateTime startDateTime = LocalDateTime.of(2017, Month.SEPTEMBER, 12, 13, 57);
        LocalDateTime toDateTime = LocalDateTime.of(2017, Month.DECEMBER, 1, 5, 7);

        DateTimePeriod dateTimePeriod = aPeriodWithToDateTime(startDateTime, toDateTime);

        assertThat(dateTimePeriod.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dateTimePeriod.getToDateTime()).isEqualTo(toDateTime);
        assertThat(dateTimePeriod.getEndDateTime()).isEqualTo(toDateTime.minusNanos(1));
    }

    @Test
    public void whenCreateDateTimePeriodThenStartDateTimeIsSameAsFromDateTime() {
        LocalDateTime startDateTime = LocalDateTime.of(2017, Month.SEPTEMBER, 12, 13, 57);

        DateTimePeriod dateTimePeriod = aPeriodWithEndDateTime(startDateTime, startDateTime);

        assertThat(dateTimePeriod.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dateTimePeriod.getFromDateTime()).isEqualTo(startDateTime);
    }

    @Test
    public void givenPeriodOfASingleDayThenNumberOfDaysIsOne() {
        LocalDateTime from = LocalDate.of(2015, JANUARY, 1).atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);

        List<LocalDate> daysInPeriod = period.getDays();

        assertThat(daysInPeriod).containsExactly(from.toLocalDate());
    }

    @Test
    public void givenPeriodOfFiveDaysThenNumberOfDaysIsFive() {
        LocalDateTime from = LocalDate.of(2015, JANUARY, 1).atStartOfDay();
        LocalDateTime to = from.plusDays(5);
        DateTimePeriod period = aPeriodWithEndDateTime(from, to);

        List<LocalDate> dagenInPeriode = period.getDays();

        assertThat(dagenInPeriode).containsExactly(
            LocalDate.of(2015, JANUARY, 1),
            LocalDate.of(2015, JANUARY, 2),
            LocalDate.of(2015, JANUARY, 3),
            LocalDate.of(2015, JANUARY, 4),
            LocalDate.of(2015, JANUARY, 5)
        );
    }
}