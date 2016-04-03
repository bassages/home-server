package nl.wiegman.home.service;

import nl.wiegman.home.model.Kosten;
import nl.wiegman.home.model.Verbruik;
import nl.wiegman.home.repository.KostenRepository;
import nl.wiegman.home.repository.MeterstandRepository;
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
public class VerbruikServiceCachedTest {

    @Mock
    MeterstandRepository meterstandRepositoryMock;

    @Mock
    KostenRepository kostenRepositoryMock;

    @InjectMocks
    VerbruikServiceCached verbruikServiceCached;

    @Test
    public void stroomverbruikPerMaandWithOneKosten() throws ParseException {
        int from = 10;
        int to = 20;

        Mockito.when(meterstandRepositoryMock.getStroomVerbruikInPeriod(from, to)).thenReturn(new BigDecimal(100));

        Verbruik verbruik = verbruikServiceCached.getVerbruikInPeriode(Energiesoort.STROOM, from, to);
        assertThat(verbruik, is(notNullValue()));
        assertThat(verbruik.getVerbruik(), is(equalTo(new BigDecimal(100d))));
    }

    @Test
    public void stroomverbruikPerMaandWithMultipleKosten() throws ParseException {
        Kosten kosten1 = new Kosten();
        kosten1.setVan(10l);
        kosten1.setTotEnMet(14l);
        kosten1.setStroomPerKwh(BigDecimal.valueOf(1));

        Kosten kosten2 = new Kosten();
        kosten2.setVan(15l);
        kosten2.setTotEnMet(100l);
        kosten2.setStroomPerKwh(BigDecimal.valueOf(2));

        when(kostenRepositoryMock.getKostenInPeriod(10, 20)).thenReturn(Arrays.asList(new Kosten[]{kosten1, kosten2}));

        when(meterstandRepositoryMock.getStroomVerbruikInPeriod(10, 14)).thenReturn(new BigDecimal(1));
        when(meterstandRepositoryMock.getStroomVerbruikInPeriod(15, 20)).thenReturn(new BigDecimal(2));

        Verbruik verbruik = verbruikServiceCached.getVerbruikInPeriode(Energiesoort.STROOM, 10, 20);
        assertThat(verbruik, is(notNullValue()));
        assertThat(verbruik.getVerbruik(), is(equalTo(new BigDecimal(3d))));
        assertThat(verbruik.getKosten().doubleValue(), is(equalTo(5.00d)));
    }

}
