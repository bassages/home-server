package nl.wiegman.homecontrol.services.service;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DateTimeUtilTest {

    @Test
    public void testThat1DayPeriodAreCorrect() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("01-01-2015");

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode.size(), is(1));
    }

    @Test
    public void testThat10DaysInPeriodAreCorrect() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("10-01-2015");

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode.size(), is(10));
    }

}
