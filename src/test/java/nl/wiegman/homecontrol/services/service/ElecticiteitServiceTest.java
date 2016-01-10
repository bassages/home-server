package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.repository.KostenRepository;
import nl.wiegman.homecontrol.services.repository.MeterstandRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ElecticiteitServiceTest {

    @Mock
    MeterstandRepository meterstandRepositoryMock;

    @Mock
    KostenRepository kostenRepository;

    @InjectMocks
    ElektriciteitService elektriciteitService;

    @Test
    public void testThat1DayPeriodAreCorrect() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("01-01-2015");

        List<Date> dagenInPeriode = elektriciteitService.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode.size(), is(1));
    }

    @Test
    public void testThat10DaysInPeriodAreCorrect() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date van = simpleDateFormat.parse("01-01-2015");
        Date totEnMet = simpleDateFormat.parse("10-01-2015");

        List<Date> dagenInPeriode = elektriciteitService.getDagenInPeriode(van.getTime(), totEnMet.getTime());
        assertThat(dagenInPeriode.size(), is(10));
    }
}
