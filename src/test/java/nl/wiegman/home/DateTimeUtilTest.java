package nl.wiegman.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class DateTimeUtilTest {

    @Test
    public void givenPeriodWithASingleDayThenNumberOfDaysIsOne() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("01-01-2015");

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode).hasSize(1);
    }

    @Test
    public void givenPeriodWithTenDaysThenNumberOfDaysIsTen() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("10-01-2015");

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode).hasSize(10);
    }

    @Test
    public void givenStartDateAfterEndDateWhenGetNumberOfDaysInPeriodThenException() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("31-12-2014");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> DateTimeUtil.getDagenInPeriode(van.getTime(), totEnMet.getTime()));
    }
}