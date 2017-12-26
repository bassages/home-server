package nl.wiegman.home.energie;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.wiegman.home.energiecontract.Energiecontract;
import nl.wiegman.home.energiecontract.EnergiecontractRepository;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class VerbruikServiceCachedTest {

    private static final int NR_OF_MILLIS_IN_ONE_HOUR = 60 * 60 * 1000;

    @Mock
    private MeterstandRepository meterstandRepositoryMock;
    @Mock
    private EnergiecontractRepository energiecontractRepositoryMock;

    @InjectMocks
    private VerbruikServiceCached verbruikServiceCached;

//    @Test
//    public void givenNoEnergiecontractsWhenGetStroomVerbruikNormaalThenNoKosten() {
//        long periodeVan = 10;
//        long periodeTotEnMet = 20;
//
//        when(meterstandRepositoryMock.getStroomVerbruikNormaalTariefInPeriod(periodeVan, periodeTotEnMet)).thenReturn(new BigDecimal("100.000"));
//
//        VerbruikKosten verbruik = verbruikServiceCached.getPotentiallyCachedStroomVerbruikInPeriode(periodeVan, periodeTotEnMet, StroomTariefIndicator.NORMAAL);
//
//        assertThat(verbruik.getVerbruik()).isEqualTo(new BigDecimal("100.000"));
//        assertThat(verbruik.getKosten()).isNull();
//    }
//
//    @Test
//    public void givenNoEnergiecontractsWhenGetStroomVerbruikDalThenNoKosten() {
//        long periodeVan = 50;
//        long periodeTotEnMet = 1900;
//
//        when(meterstandRepositoryMock.getStroomVerbruikDalTariefInPeriod(periodeVan, periodeTotEnMet)).thenReturn(new BigDecimal("100.067"));
//
//        VerbruikKosten verbruik = verbruikServiceCached.getPotentiallyCachedStroomVerbruikInPeriode(periodeVan, periodeTotEnMet, StroomTariefIndicator.DAL);
//
//        assertThat(verbruik.getVerbruik()).isEqualTo(new BigDecimal("100.067"));
//        assertThat(verbruik.getKosten()).isNull();
//    }
//
//    @Test
//    public void givenNoEnergiecontractsWhenGetGasVerbruikThenNoKosten() throws ParseException {
//        long periodeVan = 2342;
//        long periodeTotEnMet = 9070;
//
//        when(meterstandRepositoryMock.getGasVerbruikInPeriod(periodeVan + NR_OF_MILLIS_IN_ONE_HOUR, periodeTotEnMet + NR_OF_MILLIS_IN_ONE_HOUR)).thenReturn(new BigDecimal("236.791"));
//
//        VerbruikKosten verbruik = verbruikServiceCached.getPotentiallyCachedGasVerbruikInPeriode(periodeVan, periodeTotEnMet);
//
//        assertThat(verbruik.getVerbruik()).isEqualTo(new BigDecimal("236.791"));
//        assertThat(verbruik.getKosten()).isNull();
//    }
//
//    @Test
//    public void givenMultipleEnergiecontractsWhenGetStroomverbruikNormaalThenVerbruikIsCalculated() throws ParseException {
//        Energiecontract energiecontract1 = new Energiecontract();
//        energiecontract1.setVan(10L);
//        energiecontract1.setTotEnMet(14L);
//        energiecontract1.setStroomPerKwhNormaalTarief(new BigDecimal("1.000000"));
//
//        Energiecontract energiecontract2 = new Energiecontract();
//        energiecontract2.setVan(15L);
//        energiecontract2.setTotEnMet(100L);
//        energiecontract2.setStroomPerKwhNormaalTarief(new BigDecimal("2.000000"));
//
//        when(energiecontractRepositoryMock.findAllInInPeriod(10, 20)).thenReturn(asList(energiecontract1, energiecontract2));
//
//        when(meterstandRepositoryMock.getStroomVerbruikNormaalTariefInPeriod(10, 14)).thenReturn(new BigDecimal(1));
//        when(meterstandRepositoryMock.getStroomVerbruikNormaalTariefInPeriod(15, 20)).thenReturn(new BigDecimal(2));
//
//        VerbruikKosten verbruik = verbruikServiceCached.getPotentiallyCachedStroomVerbruikInPeriode(10, 20, StroomTariefIndicator.NORMAAL);
//
//        assertThat(verbruik.getVerbruik()).isEqualTo(new BigDecimal("3"));
//        assertThat(verbruik.getKosten()).isEqualTo(new BigDecimal("5.000"));
//    }
//
//    @Test
//    public void givenMultipleEnergiecontractsWhenGetStroomverbruikDalThenVerbruikIsCalculated() throws ParseException {
//        Energiecontract energiecontract1 = new Energiecontract();
//        energiecontract1.setVan(10L);
//        energiecontract1.setTotEnMet(14L);
//        energiecontract1.setStroomPerKwhNormaalTarief(null);
//        energiecontract1.setStroomPerKwhDalTarief(new BigDecimal("1.003107"));
//
//        Energiecontract energiecontract2 = new Energiecontract();
//        energiecontract2.setVan(15L);
//        energiecontract2.setTotEnMet(100L);
//        energiecontract2.setStroomPerKwhNormaalTarief(null);
//        energiecontract2.setStroomPerKwhDalTarief(new BigDecimal("2.200987"));
//
//        when(energiecontractRepositoryMock.findAllInInPeriod(10, 20)).thenReturn(asList(energiecontract1, energiecontract2));
//
//        when(meterstandRepositoryMock.getStroomVerbruikDalTariefInPeriod(10, 14)).thenReturn(new BigDecimal(1));
//        when(meterstandRepositoryMock.getStroomVerbruikDalTariefInPeriod(15, 20)).thenReturn(new BigDecimal(2));
//
//        VerbruikKosten verbruik = verbruikServiceCached.getPotentiallyCachedStroomVerbruikInPeriode(10, 20, StroomTariefIndicator.DAL);
//
//        assertThat(verbruik.getVerbruik()).isEqualTo(new BigDecimal("3"));
//        assertThat(verbruik.getKosten()).isEqualTo(new BigDecimal("5.405"));
//    }
//
//    @Test
//    public void givenMultipleEnergiecontractsWhenGetGasverbruikThenVerbruikIsCalculated() throws ParseException {
//        Energiecontract energiecontract1 = new Energiecontract();
//        energiecontract1.setVan(10L);
//        energiecontract1.setTotEnMet(14L);
//        energiecontract1.setGasPerKuub(new BigDecimal("2.404245"));
//
//        Energiecontract energiecontract2 = new Energiecontract();
//        energiecontract2.setVan(15L);
//        energiecontract2.setTotEnMet(100L);
//        energiecontract2.setGasPerKuub(new BigDecimal("3.404245"));
//
//        when(energiecontractRepositoryMock.findAllInInPeriod(10, 20)).thenReturn(asList(energiecontract1, energiecontract2));
//
//        when(meterstandRepositoryMock.getGasVerbruikInPeriod(10 + NR_OF_MILLIS_IN_ONE_HOUR, 14 + NR_OF_MILLIS_IN_ONE_HOUR)).thenReturn(new BigDecimal(4));
//        when(meterstandRepositoryMock.getGasVerbruikInPeriod(15 + NR_OF_MILLIS_IN_ONE_HOUR, 20 + NR_OF_MILLIS_IN_ONE_HOUR)).thenReturn(new BigDecimal(62));
//
//        VerbruikKosten verbruik = verbruikServiceCached.getPotentiallyCachedGasVerbruikInPeriode(10, 20);
//
//        assertThat(verbruik.getVerbruik()).isEqualTo(new BigDecimal("66"));
//        assertThat(verbruik.getKosten()).isEqualTo(new BigDecimal("220.680"));
//    }

}
