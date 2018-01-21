package nl.homeserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Test;

public class DatePeriodTest {

    @Test
    public void whenCreateDatePeriodWithEndDateThenToDateIsSet() {
        LocalDate startDate = LocalDate.of(2017, Month.SEPTEMBER, 12);
        LocalDate endDate = LocalDate.of(2017, Month.DECEMBER, 1);

        DatePeriod datePeriod = DatePeriod.aPeriodWithEndDate(startDate, endDate);

        assertThat(datePeriod.getFromDate()).isEqualTo(startDate);
        assertThat(datePeriod.getEndDate()).isEqualTo(endDate);
        assertThat(datePeriod.getToDate()).isEqualTo(endDate.plusDays(1));
    }

    @Test
    public void whenCreateDatePeriodWithToDateThenEndDateIsSet() {
        LocalDate startDate = LocalDate.of(2017, Month.SEPTEMBER, 12);
        LocalDate toDate = LocalDate.of(2017, Month.DECEMBER, 1);

        DatePeriod datePeriod = DatePeriod.aPeriodWithToDate(startDate, toDate);

        assertThat(datePeriod.getFromDate()).isEqualTo(startDate);
        assertThat(datePeriod.getToDate()).isEqualTo(toDate);
        assertThat(datePeriod.getEndDate()).isEqualTo(toDate.minusDays(1));
    }


}