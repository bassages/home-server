package nl.homeserver;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.SEPTEMBER;
import static nl.homeserver.DateTimePeriod.aPeriodWithEndDateTime;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;

public class DateTimePeriodTest {

    @Test
    public void whenCreateDateTimePeriodWithEndDateTimeThenToDateTimeIsSet() {
        final LocalDateTime fromDateTime = LocalDateTime.of(2017, SEPTEMBER, 12, 13, 57);
        final LocalDateTime endDateTime = LocalDateTime.of(2017, DECEMBER, 1, 5, 7);

        final DateTimePeriod dateTimePeriod = aPeriodWithEndDateTime(fromDateTime, endDateTime);

        assertThat(dateTimePeriod.getFromDateTime()).isEqualTo(fromDateTime);
        assertThat(dateTimePeriod.getToDateTime()).isEqualTo(endDateTime.plusNanos(1));
        assertThat(dateTimePeriod.getEndDateTime()).isEqualTo(endDateTime);
    }

    @Test
    public void whenCreateDateTimePeriodWithToDateTimeThenEndDateTimeIsSet() {
        final LocalDateTime startDateTime = LocalDateTime.of(2017, SEPTEMBER, 12, 13, 57);
        final LocalDateTime toDateTime = LocalDateTime.of(2017, DECEMBER, 1, 5, 7);

        final DateTimePeriod dateTimePeriod = aPeriodWithToDateTime(startDateTime, toDateTime);

        assertThat(dateTimePeriod.getFromDateTime()).isEqualTo(startDateTime);
        assertThat(dateTimePeriod.getToDateTime()).isEqualTo(toDateTime);
        assertThat(dateTimePeriod.getEndDateTime()).isEqualTo(toDateTime.minusNanos(1));
    }

    @Test
    public void givenPeriodOfASingleDayThenNumberOfDaysIsOne() {
        final LocalDateTime from = LocalDate.of(2015, JANUARY, 1).atStartOfDay();
        final LocalDateTime to = from.plusDays(1);
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final List<LocalDate> daysInPeriod = period.getDays();

        assertThat(daysInPeriod).containsExactly(from.toLocalDate());
    }

    @Test
    public void givenPeriodOfFiveDaysThenNumberOfDaysIsFive() {
        final LocalDateTime from = LocalDate.of(2015, JANUARY, 1).atStartOfDay();
        final LocalDateTime to = from.plusDays(5);
        final DateTimePeriod period = aPeriodWithEndDateTime(from, to);

        final List<LocalDate> dagenInPeriode = period.getDays();

        assertThat(dagenInPeriode).containsExactly(
            LocalDate.of(2015, JANUARY, 1),
            LocalDate.of(2015, JANUARY, 2),
            LocalDate.of(2015, JANUARY, 3),
            LocalDate.of(2015, JANUARY, 4),
            LocalDate.of(2015, JANUARY, 5)
        );
    }

    @Test
    public void givenPeriodThatStartsOnGivenDateTimeWhengetStartsOnOrAfterThenTrue() {
        final LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();

        final DateTimePeriod period = aPeriodWithEndDateTime(start, start.plusDays(10));

        assertThat(period.startsOnOrAfter(start)).isTrue();
    }

    @Test
    public void givenPeriodThatStartsAfterGivenDateTimeWhengetStartsOnOrAfterThenTrue() {
        final LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();

        final DateTimePeriod period = aPeriodWithEndDateTime(start, start.plusDays(10));

        assertThat(period.startsOnOrAfter(start.minusMinutes(1))).isTrue();
    }

    @Test
    public void givenPeriodThatStartsBeforeGivenDateTimeWhengetStartsOnOrAfterThenTrue() {
        final LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();

        final DateTimePeriod period = aPeriodWithEndDateTime(start, start.plusDays(10));

        assertThat(period.startsOnOrAfter(start.plusMinutes(1))).isFalse();
    }

    @Test
    public void whenToStringThenStringRepresentationIsReturned() {
        final LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();

        final DateTimePeriod period = aPeriodWithEndDateTime(start, start.plusDays(10));

        assertThat(period.toString()).isEqualTo("DateTimePeriod(startDateTime=2015-01-01T00:00, endDateTime=2015-01-11T00:00, toDateTime=2015-01-11T00:00:00.000000001)");
    }
}