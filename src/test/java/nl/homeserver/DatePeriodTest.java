package nl.homeserver;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class DatePeriodTest {

    @Test
    void whenCreateDatePeriodWithEndDateThenToDateIsSet() {
        final LocalDate startDate = LocalDate.of(2017, Month.SEPTEMBER, 12);
        final LocalDate endDate = LocalDate.of(2017, Month.DECEMBER, 1);

        final DatePeriod datePeriod = DatePeriod.aPeriodWithEndDate(startDate, endDate);

        assertThat(datePeriod.getFromDate()).isEqualTo(startDate);
        assertThat(datePeriod.getEndDate()).isEqualTo(endDate);
        assertThat(datePeriod.getToDate()).isEqualTo(endDate.plusDays(1));
    }

    @Test
    void whenCreateDatePeriodWithToDateThenEndDateIsSet() {
        final LocalDate fromDate = LocalDate.of(2017, Month.SEPTEMBER, 12);
        final LocalDate toDate = LocalDate.of(2017, Month.DECEMBER, 1);

        final DatePeriod datePeriod = DatePeriod.aPeriodWithToDate(fromDate, toDate);

        assertThat(datePeriod.getFromDate()).isEqualTo(fromDate);
        assertThat(datePeriod.getToDate()).isEqualTo(toDate);
        assertThat(datePeriod.getEndDate()).isEqualTo(toDate.minusDays(1));
    }

    @Test
    void givenTwoWeekThenNumberOfWeekendDaysIsFour() {
        final LocalDate fromDate = LocalDate.of(2017, Month.SEPTEMBER, 12);
        final LocalDate toDate = fromDate.plusDays(14);

        final DatePeriod datePeriod = DatePeriod.aPeriodWithToDate(fromDate, toDate);

        final long numberOfWeekendDays = datePeriod.getNumberOfWeekendDays();

        assertThat(numberOfWeekendDays).isEqualTo(4);
    }

    @Test
    void givenTwoWeekThenNumberOfWeekDaysIsTen() {
        final LocalDate fromDate = LocalDate.of(2017, Month.SEPTEMBER, 12);
        final LocalDate toDate = fromDate.plusDays(14);

        final DatePeriod datePeriod = DatePeriod.aPeriodWithToDate(fromDate, toDate);

        final long numberOfWeekDays = datePeriod.getNumberOfWeekDays();

        assertThat(numberOfWeekDays).isEqualTo(10);
    }

    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(DatePeriod.class).verify();
    }
}
