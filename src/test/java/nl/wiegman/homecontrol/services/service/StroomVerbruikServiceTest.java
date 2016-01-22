package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.model.api.Stroomverbruik;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import nl.wiegman.homecontrol.services.repository.MeterstandRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StroomVerbruikServiceTest {

    @Mock
    MeterstandRepository meterstandRepositoryMock;

    @Mock
    KostenRepository kostenRepositoryMock;

    @InjectMocks
    StroomVerbruikService stroomVerbruikService;

    @Test
    public void stroomverbruikPerMaandWithOneKosten() throws ParseException {
        int from = 10;
        int to = 20;

        Mockito.when(meterstandRepositoryMock.getVerbruikInPeriod(from, to)).thenReturn(100);

        Stroomverbruik stroomverbruik = stroomVerbruikService.getVerbruikInPeriode(from, to);
        assertThat(stroomverbruik, is(notNullValue()));
        assertThat(stroomverbruik.getkWh(), is(equalTo(100)));
    }

    @Test
    public void stroomverbruikPerMaandWithMultipleKosten() throws ParseException {
        Kosten kosten1 = new Kosten();
        kosten1.setVan(10);
        kosten1.setTotEnMet(14);
        kosten1.setStroomPerKwh(BigDecimal.valueOf(1));

        Kosten kosten2 = new Kosten();
        kosten2.setVan(15);
        kosten2.setTotEnMet(100);
        kosten2.setStroomPerKwh(BigDecimal.valueOf(2));

        when(kostenRepositoryMock.getKostenInPeriod(10, 20)).thenReturn(Arrays.asList(new Kosten[]{kosten1, kosten2}));

        when(meterstandRepositoryMock.getVerbruikInPeriod(10, 14)).thenReturn(1);
        when(meterstandRepositoryMock.getVerbruikInPeriod(15, 20)).thenReturn(2);

        Stroomverbruik stroomverbruik = stroomVerbruikService.getVerbruikInPeriode(10, 20);
        assertThat(stroomverbruik, is(notNullValue()));
        assertThat(stroomverbruik.getkWh(), is(equalTo(3)));
        assertThat(stroomverbruik.getEuro().doubleValue(), is(equalTo(5.00d)));
    }

}
