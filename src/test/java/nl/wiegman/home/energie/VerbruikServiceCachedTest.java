package nl.wiegman.home.energie;

import nl.wiegman.home.energiecontract.Energiecontract;
import nl.wiegman.home.energiecontract.EnergiecontractRepository;

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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VerbruikServiceCachedTest {

    @Mock
    private MeterstandRepository meterstandRepositoryMock;
    @Mock
    private EnergiecontractRepository energiecontractRepositoryMock;

    @InjectMocks
    private VerbruikServiceCached verbruikServiceCached;

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
        Energiecontract energiecontract1 = new Energiecontract();
        energiecontract1.setVan(10L);
        energiecontract1.setTotEnMet(14L);
        energiecontract1.setStroomPerKwhNormaalTarief(BigDecimal.valueOf(1));

        Energiecontract energiecontract2 = new Energiecontract();
        energiecontract2.setVan(15L);
        energiecontract2.setTotEnMet(100L);
        energiecontract2.setStroomPerKwhNormaalTarief(BigDecimal.valueOf(2));

        when(energiecontractRepositoryMock.findAllInInPeriod(10, 20)).thenReturn(Arrays.asList(energiecontract1, energiecontract2));

        when(meterstandRepositoryMock.getStroomVerbruikInPeriod(10, 14)).thenReturn(new BigDecimal(1));
        when(meterstandRepositoryMock.getStroomVerbruikInPeriod(15, 20)).thenReturn(new BigDecimal(2));

        Verbruik verbruik = verbruikServiceCached.getVerbruikInPeriode(Energiesoort.STROOM, 10, 20);
        assertThat(verbruik, is(notNullValue()));
        assertThat(verbruik.getVerbruik(), is(equalTo(new BigDecimal(3d))));
        assertThat(verbruik.getKosten().doubleValue(), is(equalTo(5.00d)));
    }

}
