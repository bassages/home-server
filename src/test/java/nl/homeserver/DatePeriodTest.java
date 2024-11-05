package nl.homeserver;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.time.Month.DECEMBER;
import static java.time.Month.SEPTEMBER;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static org.assertj.core.api.Assertions.assertThat;

class DatePeriodTest {

    @Test
    void whenCreateDatePeriodWithEndDateThenToDateIsSet() {
        // given
        final LocalDate startDate = LocalDate.of(2017, SEPTEMBER, 12);
        final LocalDate endDate = LocalDate.of(2017, DECEMBER, 1);

        // when
        final DatePeriod datePeriod = DatePeriod.aPeriodWithEndDate(startDate, endDate);

        // then
        assertThat(datePeriod.getFromDate()).isEqualTo(startDate);
        assertThat(datePeriod.getEndDate()).isEqualTo(endDate);
        assertThat(datePeriod.getToDate()).isEqualTo(endDate.plusDays(1));
    }

    @Test
    void whenCreateDatePeriodWithToDateThenEndDateIsSet() {
        // given
        final LocalDate fromDate = LocalDate.of(2017, SEPTEMBER, 12);
        final LocalDate toDate = LocalDate.of(2017, DECEMBER, 1);

        // when
        final DatePeriod datePeriod = aPeriodWithToDate(fromDate, toDate);

        // then
        assertThat(datePeriod.getFromDate()).isEqualTo(fromDate);
        assertThat(datePeriod.getToDate()).isEqualTo(toDate);
        assertThat(datePeriod.getEndDate()).isEqualTo(toDate.minusDays(1));
    }

    @Test
    void givenTwoWeekThenNumberOfWeekendDaysIsFour() {
        // given
        final LocalDate fromDate = LocalDate.of(2017, SEPTEMBER, 12);
        final LocalDate toDate = fromDate.plusDays(14);
        final DatePeriod datePeriod = aPeriodWithToDate(fromDate, toDate);

        // when
        final long numberOfWeekendDays = datePeriod.getNumberOfWeekendDays();

        // then
        assertThat(numberOfWeekendDays).isEqualTo(4);
    }

    @Test
    void givenTwoWeekThenNumberOfWeekDaysIsTen() {
        // given
        final LocalDate fromDate = LocalDate.of(2017, SEPTEMBER, 12);
        final LocalDate toDate = fromDate.plusDays(14);
        final DatePeriod datePeriod = aPeriodWithToDate(fromDate, toDate);

        // when
        final long numberOfWeekDays = datePeriod.getNumberOfWeekDays();

        // then
        assertThat(numberOfWeekDays).isEqualTo(10);
    }

    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(DatePeriod.class).verify();
    }
}
