package nl.wiegman.home;

import static java.time.Month.JANUARY;
import static nl.wiegman.home.DateTimePeriod.aPeriodWithEndDateTime;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;

public class DateTimeUtilTest {

    @Test
    public void givenPeriodWithASingleDayThenNumberOfDaysIsOne() {
        LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(2015, JANUARY, 1).atStartOfDay();
        DateTimePeriod period = aPeriodWithEndDateTime(start, end);

        List<LocalDate> dagenInPeriode = DateTimeUtil.getDaysInPeriod(period);

        assertThat(dagenInPeriode).hasSize(1);
    }

    @Test
    public void givenPeriodWithTenDaysThenNumberOfDaysIsTen() {
        LocalDateTime start = LocalDate.of(2015, JANUARY, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(2015, JANUARY, 10).atStartOfDay();
        DateTimePeriod period = aPeriodWithEndDateTime(start, end);

        List<LocalDate> dagenInPeriode = DateTimeUtil.getDaysInPeriod(period);

        assertThat(dagenInPeriode).hasSize(10);
    }
}