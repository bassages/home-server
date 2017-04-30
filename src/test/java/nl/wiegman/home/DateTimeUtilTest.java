package nl.wiegman.home;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import nl.wiegman.home.DateTimeUtil;

public class DateTimeUtilTest {

    @Test
    public void singleDayPeriodIsCorrect() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("01-01-2015");

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode.size(), is(1));
    }

    @Test
    public void tenDayPeriodIsCorrect() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("10-01-2015");

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode.size(), is(10));
    }

    @Test
    public void toMustNotBeBeforeThenFrom() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("31-12-2014");

        assertThatThrownBy(() -> {
            DateTimeUtil.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        }).isInstanceOf(RuntimeException.class);
    }
}