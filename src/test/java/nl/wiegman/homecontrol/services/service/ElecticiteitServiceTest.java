package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.model.api.StroomVerbruikPerMaandInJaar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
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

    @Test
    public void stroomverbruikPerMaandWithOneKosten() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        Mockito.when(meterstandRepositoryMock.getVerbruikInPeriod(sdf.parse("01-01-2016 00:00:00.000").getTime(), sdf.parse("31-01-2016 23:59:59.999").getTime())).thenReturn(100);

        StroomVerbruikPerMaandInJaar stroomverbruikInMaand = elektriciteitService.getStroomverbruikInMaand(1, 2016);
        assertThat(stroomverbruikInMaand, is(notNullValue()));
        assertThat(stroomverbruikInMaand.getkWh(), is(equalTo(100)));
    }

    @Test
    public void stroomverbruikPerMaandWithMultipleKosten() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        Mockito.when(meterstandRepositoryMock.getVerbruikInPeriod(sdf.parse("01-01-2016 00:00:00.000").getTime(), sdf.parse("01-01-2016 23:59:59.999").getTime())).thenReturn(1);
        Mockito.when(meterstandRepositoryMock.getVerbruikInPeriod(sdf.parse("02-01-2016 00:00:00.000").getTime(), sdf.parse("31-01-2016 23:59:59.999").getTime())).thenReturn(2);

        Kosten kosten1 = new Kosten();
        kosten1.setStroomPerKwh(BigDecimal.valueOf(1));
        kosten1.setVan(sdf.parse("01-01-2015 00:00:00.000").getTime());
        kosten1.setTot(sdf.parse("01-01-2016 23:59:59.999").getTime());

        Kosten kosten2 = new Kosten();
        kosten2.setStroomPerKwh(BigDecimal.valueOf(2));
        kosten2.setVan(sdf.parse("02-01-2016 00:00:00.000").getTime());
        kosten2.setTot(sdf.parse("01-01-2017 00:00:00.000").getTime());

        Mockito.when(kostenRepository.getKostenInPeriod(sdf.parse("01-01-2016 00:00:00.000").getTime(), sdf.parse("01-02-2016 00:00:00.000").getTime())).thenReturn(Arrays.asList(new Kosten[] {kosten1, kosten2}));

        StroomVerbruikPerMaandInJaar stroomverbruikInMaand = elektriciteitService.getStroomverbruikInMaand(1, 2016);
        assertThat(stroomverbruikInMaand, is(notNullValue()));
        assertThat(stroomverbruikInMaand.getkWh(), is(equalTo(3)));
        assertThat(stroomverbruikInMaand.getEuro().doubleValue(), is(equalTo(5.00d)));
    }

}
