package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.service.ElektriciteitService;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ElecticiteitServiceTest {

    @Test
    public void testThat1DayPeriodAreCorrect() throws ParseException {
        ElektriciteitService elektriciteitService = new ElektriciteitService();

        Date van = new SimpleDateFormat("dd-MM-yyyy").parse("01-01-2015");
        Date totEnMet = new SimpleDateFormat("dd-MM-yyyy").parse("01-01-2015");

        List<Date> dagenInPeriode = elektriciteitService.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode.size(), is(1));
    }

    @Test
    public void testThat10DaysInPeriodAreCorrect() throws ParseException {
        ElektriciteitService elektriciteitService = new ElektriciteitService();

        Date van = new SimpleDateFormat("dd-MM-yyyy").parse("01-01-2015");
        Date totEnMet = new SimpleDateFormat("dd-MM-yyyy").parse("10-01-2015");

        List<Date> dagenInPeriode = elektriciteitService.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode.size(), is(10));
    }
}
