package nl.wiegman.home.energie;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.HOURS;
import static nl.wiegman.home.DatePeriod.aPeriodWithEndDate;
import static nl.wiegman.home.DatePeriod.aPeriodWithToDate;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpochAtStartOfDay;
import static nl.wiegman.home.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import nl.wiegman.home.DatePeriod;
import nl.wiegman.home.energiecontract.Energiecontract;
import nl.wiegman.home.energiecontract.EnergiecontractService;

@RunWith(MockitoJUnitRunner.class)
public class VerbruikServiceTest {
    private static final long NR_OF_MILLIS_IN_ONE_HOUR = HOURS.toMillis(1);

    private VerbruikService verbruikService;

    @Mock
    private MeterstandService meterstandService;
    @Mock
    private EnergiecontractService energiecontractService;
    @Mock
    private VerbruikRepository verbruikRepository;

    private void createVerbruikService(Clock clock) {
        verbruikService = new VerbruikService(meterstandService, energiecontractService, verbruikRepository, clock);
        ReflectionTestUtils.setField(verbruikService, "verbruikServiceProxyWithEnabledCaching", verbruikService);
    }

    @Test
    public void whenGetVerbruikPerDagForDateInFutureThenNoOtherServicesCalledAndUsageIsZero() {
        createVerbruikService(timeTravelTo(LocalDate.of(2016, JANUARY, 1).atStartOfDay()));

        LocalDate from = LocalDate.of(2016, JANUARY, 2);
        LocalDate to = LocalDate.of(2016, JANUARY, 3);
        DatePeriod period = aPeriodWithToDate(from, to);

        List<VerbruikKostenOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        verifyZeroInteractions(meterstandService);

        assertThat(verbruikPerDag).hasSize(1);
        VerbruikKostenOpDag verbruikKostenOpDag = verbruikPerDag.get(0);
        assertThat(verbruikKostenOpDag.getDag()).isEqualTo(from);
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getGasKosten()).isNull();
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getGasVerbruik()).isNull();
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getStroomKostenDal()).isNull();
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getStroomVerbruikDal()).isNull();
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getStroomKostenNormaal()).isNull();
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getStroomVerbruikNormaal()).isNull();
    }

    @Test
    public void givenMultipleEnergycontractsWhenGetVerbruikPerDagForTwoDaysInThePastThenUsagesAndCostsAreRetrievedFromCacheAndCostsAreCalculatedBasedOnValidEnergycontract() {
        createVerbruikService(timeTravelTo(LocalDate.of(2016, JANUARY, 4).atStartOfDay()));

        LocalDate from = LocalDate.of(2016, JANUARY, 2);
        LocalDate to = LocalDate.of(2016, JANUARY, 4);
        DatePeriod period = aPeriodWithToDate(from, to);

        Energiecontract energiecontract1 = new Energiecontract();
        energiecontract1.setVan(0L);
        energiecontract1.setTotEnMet(toMillisSinceEpochAtStartOfDay(from.plusDays(1)) - 1);
        energiecontract1.setGasPerKuub(new BigDecimal("10"));
        energiecontract1.setStroomPerKwhDalTarief(new BigDecimal("20"));
        energiecontract1.setStroomPerKwhNormaalTarief(new BigDecimal("30"));

        Energiecontract energiecontract2 = new Energiecontract();
        energiecontract2.setVan(toMillisSinceEpochAtStartOfDay(from.plusDays(1)));
        energiecontract2.setTotEnMet(Long.MAX_VALUE);
        energiecontract2.setGasPerKuub(new BigDecimal("40"));
        energiecontract2.setStroomPerKwhDalTarief(new BigDecimal("50"));
        energiecontract2.setStroomPerKwhNormaalTarief(new BigDecimal("60"));

        when(energiecontractService.findAllInInPeriod(any())).thenReturn(asList(energiecontract1, energiecontract2));

        when(verbruikRepository.getGasVerbruikInPeriod(toMillisSinceEpochAtStartOfDay(from) + NR_OF_MILLIS_IN_ONE_HOUR, toMillisSinceEpochAtStartOfDay(from.plusDays(1)) - 1 + NR_OF_MILLIS_IN_ONE_HOUR))
                .thenReturn(new BigDecimal("1.111"));
        when(verbruikRepository.getStroomVerbruikDalTariefInPeriod(toMillisSinceEpochAtStartOfDay(from), toMillisSinceEpochAtStartOfDay(from.plusDays(1)) - 1))
                .thenReturn(new BigDecimal("2.222"));
        when(verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(toMillisSinceEpochAtStartOfDay(from), toMillisSinceEpochAtStartOfDay(from.plusDays(1)) - 1))
                .thenReturn(new BigDecimal("3.333"));

        when(verbruikRepository.getGasVerbruikInPeriod(toMillisSinceEpochAtStartOfDay(from.plusDays(1)) + NR_OF_MILLIS_IN_ONE_HOUR, toMillisSinceEpochAtStartOfDay(from.plusDays(2)) - 1 + NR_OF_MILLIS_IN_ONE_HOUR))
                .thenReturn(new BigDecimal("1.999"));
        when(verbruikRepository.getStroomVerbruikDalTariefInPeriod(toMillisSinceEpochAtStartOfDay(from.plusDays(1)), toMillisSinceEpochAtStartOfDay(from.plusDays(2)) - 1))
                .thenReturn(new BigDecimal("2.888"));
        when(verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(toMillisSinceEpochAtStartOfDay(from.plusDays(1)), toMillisSinceEpochAtStartOfDay(from.plusDays(2)) - 1))
                .thenReturn(new BigDecimal("3.777"));

        List<VerbruikKostenOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        assertThat(verbruikPerDag).hasSize(2);
        VerbruikKostenOpDag verbruikKostenOpDag1 = verbruikPerDag.get(0);
        assertThat(verbruikKostenOpDag1.getDag()).isEqualTo(from);
        assertThat(verbruikKostenOpDag1.getVerbruikKostenOverzicht().getGasVerbruik()).isEqualTo(new BigDecimal("1.111"));
        assertThat(verbruikKostenOpDag1.getVerbruikKostenOverzicht().getGasKosten()).isEqualTo(new BigDecimal("11.110"));
        assertThat(verbruikKostenOpDag1.getVerbruikKostenOverzicht().getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.222"));
        assertThat(verbruikKostenOpDag1.getVerbruikKostenOverzicht().getStroomKostenDal()).isEqualTo(new BigDecimal("44.440"));
        assertThat(verbruikKostenOpDag1.getVerbruikKostenOverzicht().getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.333"));
        assertThat(verbruikKostenOpDag1.getVerbruikKostenOverzicht().getStroomKostenNormaal()).isEqualTo(new BigDecimal("99.990"));

        VerbruikKostenOpDag verbruikKostenOpDag2 = verbruikPerDag.get(1);
        assertThat(verbruikKostenOpDag2.getDag()).isEqualTo(from.plusDays(1));
        assertThat(verbruikKostenOpDag2.getVerbruikKostenOverzicht().getGasVerbruik()).isEqualTo(new BigDecimal("1.999"));
        assertThat(verbruikKostenOpDag2.getVerbruikKostenOverzicht().getGasKosten()).isEqualTo(new BigDecimal("79.960"));
        assertThat(verbruikKostenOpDag2.getVerbruikKostenOverzicht().getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.888"));
        assertThat(verbruikKostenOpDag2.getVerbruikKostenOverzicht().getStroomKostenDal()).isEqualTo(new BigDecimal("144.400"));
        assertThat(verbruikKostenOpDag2.getVerbruikKostenOverzicht().getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.777"));
        assertThat(verbruikKostenOpDag2.getVerbruikKostenOverzicht().getStroomKostenNormaal()).isEqualTo(new BigDecimal("226.620"));
    }

    @Test
    public void whenGetVerbruikPerDagForCurrentDayThenUsageAreRetrievedFromNonCachedService() {
        createVerbruikService(timeTravelTo(LocalDate.of(2016, JANUARY, 4).atTime(14, 43, 13)));

        VerbruikService verbruikServiceProxyWithEnabledCaching = mock(VerbruikService.class);
        ReflectionTestUtils.setField(verbruikService, "verbruikServiceProxyWithEnabledCaching", verbruikServiceProxyWithEnabledCaching);

        LocalDate day = LocalDate.of(2016, JANUARY, 4);
        DatePeriod period = aPeriodWithEndDate(day, day);

        Energiecontract energiecontract = new Energiecontract();
        energiecontract.setVan(0L);
        energiecontract.setTotEnMet(Long.MAX_VALUE);
        energiecontract.setGasPerKuub(new BigDecimal("10"));
        energiecontract.setStroomPerKwhDalTarief(new BigDecimal("20"));
        energiecontract.setStroomPerKwhNormaalTarief(new BigDecimal("30"));
        when(energiecontractService.findAllInInPeriod(any())).thenReturn(singletonList(energiecontract));

        when(verbruikRepository.getGasVerbruikInPeriod(toMillisSinceEpochAtStartOfDay(day) + NR_OF_MILLIS_IN_ONE_HOUR, toMillisSinceEpochAtStartOfDay(day.plusDays(1)) - 1 + NR_OF_MILLIS_IN_ONE_HOUR))
                .thenReturn(new BigDecimal("1.000"));
        when(verbruikRepository.getStroomVerbruikDalTariefInPeriod(toMillisSinceEpochAtStartOfDay(day), toMillisSinceEpochAtStartOfDay(day.plusDays(1)) - 1))
                .thenReturn(new BigDecimal("2.000"));
        when(verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(toMillisSinceEpochAtStartOfDay(day), toMillisSinceEpochAtStartOfDay(day.plusDays(1)) - 1))
                .thenReturn(new BigDecimal("3.000"));

        List<VerbruikKostenOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        assertThat(verbruikPerDag).hasSize(1);
        VerbruikKostenOpDag verbruikKostenOpDag = verbruikPerDag.get(0);
        assertThat(verbruikKostenOpDag.getDag()).isEqualTo(day);
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getGasVerbruik()).isEqualTo(new BigDecimal("1.000"));
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getGasKosten()).isEqualTo(new BigDecimal("10.000"));
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.000"));
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getStroomKostenDal()).isEqualTo(new BigDecimal("40.000"));
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.000"));
        assertThat(verbruikKostenOpDag.getVerbruikKostenOverzicht().getStroomKostenNormaal()).isEqualTo(new BigDecimal("90.000"));

        verifyZeroInteractions(verbruikServiceProxyWithEnabledCaching);
    }
}