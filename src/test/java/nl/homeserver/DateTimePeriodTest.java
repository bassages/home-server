package nl.homeserver;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.*;
import static nl.homeserver.DateTimePeriod.aPeriodWithEndDateTime;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class DateTimePeriodTest {

    @Test
    void whenCreateDateTimePeriodWithEndDateTimeThenToDateTimeIsSet() {
        final LocalDateTime fromDateTime = LocalDateTime.of(2017, SEPTEMBER, 12, 13, 57);
        final LocalDateTime endDateTime = LocalDateTime.of(2017, DECEMBER, 1, 5, 7);

        final DateTimePeriod dateTimePeriod = aPeriodWithEndDateTime(fromDateTime, endDateTime);

        assertThat(dateTimePeriod.getFromDateTime()).isEqualTo(fromDateTime);
        assertThat(dateTimePeriod.getToDateTime()).isEqualTo(endDateTime.plusNanos(1));
        assertThat(dateTimePeriod.getEndDateTime()).isEqualTo(endDateTime);
    }

    @Test
    void whenCreateDateTimePeriodWithToDateTimeThenEndDateTimeIsSet() {
        final LocalDateTime startDateTime = LocalDateTime.of(2017, SEPTEMBER, 12, 13, 57);
        final LocalDateTime toDateTime = LocalDateTime.of(2017, DECEMBER, 1, 5, 7);

        final DateTimePeriod dateTimePeriod = aPeriodWithToDateTime(startDateTime, toDateTime);

        assertThat(dateTimePeriod.getFromDateTime()).isEqualTo(startDateTime);
        assertThat(dateTimePeriod.getToDateTime()).isEqualTo(toDateTime);
        assertThat(dateTimePeriod.getEndDateTime()).isEqualTo(toDateTime.minusNanos(1));
    }

    @Test
    void givenPeriodOfASingleDayThenNumberOfDaysIsOne() {
        final LocalDateTime from = LocalDate.of(2015, JANUARY, 1).atStartOfDay();
        final LocalDateTime to = from.plusDays(1);
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final List<LocalDate> daysInPeriod = period.getDays();

        assertThat(daysInPeriod).containsExactly(from.toLocalDate());
    }

    @Test
    void givenPeriodOfFiveDaysThenNumberOfDaysIsFive() {
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
    void givenPeriodThatStartsOnGivenDateTimeWhengetStartsOnOrAfterThenTrue() {
        final LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();

        final DateTimePeriod period = aPeriodWithEndDateTime(start, start.plusDays(10));

        assertThat(period.startsOnOrAfter(start)).isTrue();
    }

    @Test
    void givenPeriodThatStartsAfterGivenDateTimeWhengetStartsOnOrAfterThenTrue() {
        final LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();

        final DateTimePeriod period = aPeriodWithEndDateTime(start, start.plusDays(10));

        assertThat(period.startsOnOrAfter(start.minusMinutes(1))).isTrue();
    }

    @Test
    void givenPeriodThatStartsBeforeGivenDateTimeWhengetStartsOnOrAfterThenTrue() {
        final LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();

        final DateTimePeriod period = aPeriodWithEndDateTime(start, start.plusDays(10));

        assertThat(period.startsOnOrAfter(start.plusMinutes(1))).isFalse();
    }

    @Test
    void whenToStringThenStringRepresentationIsReturned() {
        final LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();

        final DateTimePeriod period = aPeriodWithEndDateTime(start, start.plusDays(10));

        assertThat(period).hasToString("DateTimePeriod(startDateTime=2015-01-01T00:00, endDateTime=2015-01-11T00:00, toDateTime=2015-01-11T00:00:00.000000001)");
    }
}
