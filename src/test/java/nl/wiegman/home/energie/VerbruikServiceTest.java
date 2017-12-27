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

        List<VerbruikOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        verifyZeroInteractions(meterstandService);

        assertThat(verbruikPerDag).hasSize(1);
        VerbruikOpDag verbruikOpDag = verbruikPerDag.get(0);
        assertThat(verbruikOpDag.getDatumtijd()).isEqualTo(toMillisSinceEpochAtStartOfDay(from));
        assertThat(verbruikOpDag.getGasKosten()).isNull();
        assertThat(verbruikOpDag.getGasVerbruik()).isNull();
        assertThat(verbruikOpDag.getStroomKostenDal()).isNull();
        assertThat(verbruikOpDag.getStroomVerbruikDal()).isNull();
        assertThat(verbruikOpDag.getStroomKostenNormaal()).isNull();
        assertThat(verbruikOpDag.getStroomVerbruikNormaal()).isNull();
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

        List<VerbruikOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        assertThat(verbruikPerDag).hasSize(2);
        VerbruikOpDag verbruikOpDag1 = verbruikPerDag.get(0);
        assertThat(verbruikOpDag1.getDatumtijd()).isEqualTo(toMillisSinceEpochAtStartOfDay(from));
        assertThat(verbruikOpDag1.getGasVerbruik()).isEqualTo(new BigDecimal("1.111"));
        assertThat(verbruikOpDag1.getGasKosten()).isEqualTo(new BigDecimal("11.110"));
        assertThat(verbruikOpDag1.getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.222"));
        assertThat(verbruikOpDag1.getStroomKostenDal()).isEqualTo(new BigDecimal("44.440"));
        assertThat(verbruikOpDag1.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.333"));
        assertThat(verbruikOpDag1.getStroomKostenNormaal()).isEqualTo(new BigDecimal("99.990"));

        VerbruikOpDag verbruikOpDag2 = verbruikPerDag.get(1);
        assertThat(verbruikOpDag2.getDatumtijd()).isEqualTo(toMillisSinceEpochAtStartOfDay(from.plusDays(1)));
        assertThat(verbruikOpDag2.getGasVerbruik()).isEqualTo(new BigDecimal("1.999"));
        assertThat(verbruikOpDag2.getGasKosten()).isEqualTo(new BigDecimal("79.960"));
        assertThat(verbruikOpDag2.getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.888"));
        assertThat(verbruikOpDag2.getStroomKostenDal()).isEqualTo(new BigDecimal("144.400"));
        assertThat(verbruikOpDag2.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.777"));
        assertThat(verbruikOpDag2.getStroomKostenNormaal()).isEqualTo(new BigDecimal("226.620"));
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

        List<VerbruikOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        assertThat(verbruikPerDag).hasSize(1);
        VerbruikOpDag verbruikOpDag = verbruikPerDag.get(0);
        assertThat(verbruikOpDag.getDatumtijd()).isEqualTo(toMillisSinceEpochAtStartOfDay(day));
        assertThat(verbruikOpDag.getGasVerbruik()).isEqualTo(new BigDecimal("1.000"));
        assertThat(verbruikOpDag.getGasKosten()).isEqualTo(new BigDecimal("10.000"));
        assertThat(verbruikOpDag.getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.000"));
        assertThat(verbruikOpDag.getStroomKostenDal()).isEqualTo(new BigDecimal("40.000"));
        assertThat(verbruikOpDag.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.000"));
        assertThat(verbruikOpDag.getStroomKostenNormaal()).isEqualTo(new BigDecimal("90.000"));

        verifyZeroInteractions(verbruikServiceProxyWithEnabledCaching);
    }
}