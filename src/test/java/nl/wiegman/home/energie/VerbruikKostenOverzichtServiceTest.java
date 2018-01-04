package nl.wiegman.home.energie;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static nl.wiegman.home.DateTimePeriod.aPeriodWithToDateTime;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpoch;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpochAtStartOfDay;
import static nl.wiegman.home.util.TimeMachine.timeTravelTo;
import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_HOUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.wiegman.home.DateTimePeriod;
import nl.wiegman.home.energiecontract.Energiecontract;
import nl.wiegman.home.energiecontract.EnergiecontractService;

@RunWith(MockitoJUnitRunner.class)
public class VerbruikKostenOverzichtServiceTest {

    @InjectMocks
    private VerbruikKostenOverzichtService verbruikKostenOverzichtService;

    @Mock
    private VerbruikRepository verbruikRepository;
    @Mock
    private EnergiecontractService energiecontractService;
    @Mock
    private Clock clock;

    @Before
    public void setup() {
        setField(verbruikKostenOverzichtService, "verbruikKostenOverzichtServiceProxyWithEnabledCaching", verbruikKostenOverzichtService);
    }

    @Test
    public void whenGetForPeriodInFutureThenNoOtherServicesCalledAndUsageIsZero() {
        timeTravelTo(clock, LocalDate.of(2016, JANUARY, 1).atStartOfDay());

        LocalDateTime from = LocalDateTime.of(2016, JANUARY, 2, 10, 6);
        LocalDateTime to = LocalDateTime.of(2016, JANUARY, 3, 4, 13);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);

        VerbruikKostenOverzicht verbruikKostenOverzicht = verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period);

        assertThat(verbruikKostenOverzicht.getGasKosten()).isNull();
        assertThat(verbruikKostenOverzicht.getGasVerbruik()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomKostenDal()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomVerbruikDal()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomKostenNormaal()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomVerbruikNormaal()).isNull();

        verifyZeroInteractions(verbruikRepository, energiecontractService);
    }

    @Test
    public void givenMultipleEnergycontractsWhenGetForPeriodInThePastThenUsagesAndCostsAreRetrievedFromCacheAndCostsAreCalculatedBasedOnValidEnergycontract() {
        timeTravelTo(clock, LocalDate.of(2016, JANUARY, 4).atStartOfDay());

        LocalDateTime from = LocalDate.of(2016, JANUARY, 2).atTime(0, 0);
        LocalDateTime to = LocalDate.of(2016, JANUARY, 4).atTime(0, 0);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);

        Energiecontract energiecontract1 = new Energiecontract();
        energiecontract1.setVan(0L);
        energiecontract1.setTotEnMet(toMillisSinceEpoch(from.plusDays(1)) - 1);
        energiecontract1.setGasPerKuub(new BigDecimal("10"));
        energiecontract1.setStroomPerKwhDalTarief(new BigDecimal("20"));
        energiecontract1.setStroomPerKwhNormaalTarief(new BigDecimal("30"));

        Energiecontract energiecontract2 = new Energiecontract();
        energiecontract2.setVan(toMillisSinceEpoch(from.plusDays(1)));
        energiecontract2.setTotEnMet(Long.MAX_VALUE);
        energiecontract2.setGasPerKuub(new BigDecimal("40"));
        energiecontract2.setStroomPerKwhDalTarief(new BigDecimal("50"));
        energiecontract2.setStroomPerKwhNormaalTarief(new BigDecimal("60"));

        when(energiecontractService.findAllInInPeriod(any())).thenReturn(asList(energiecontract1, energiecontract2));

        when(verbruikRepository.getGasVerbruikInPeriod(toMillisSinceEpoch(from) + MILLIS_PER_HOUR, toMillisSinceEpoch(from.plusDays(1)) - 1 + MILLIS_PER_HOUR))
                .thenReturn(new BigDecimal("1.111"));
        when(verbruikRepository.getStroomVerbruikDalTariefInPeriod(toMillisSinceEpoch(from), toMillisSinceEpoch(from.plusDays(1)) - 1))
                .thenReturn(new BigDecimal("2.222"));
        when(verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(toMillisSinceEpoch(from), toMillisSinceEpoch(from.plusDays(1)) - 1))
                .thenReturn(new BigDecimal("3.333"));

        VerbruikKostenOverzicht verbruikKostenOverzicht = verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period);

        assertThat(verbruikKostenOverzicht.getGasVerbruik()).isEqualTo(new BigDecimal("1.111"));
        assertThat(verbruikKostenOverzicht.getGasKosten()).isEqualTo(new BigDecimal("11.110"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.222"));
        assertThat(verbruikKostenOverzicht.getStroomKostenDal()).isEqualTo(new BigDecimal("44.440"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.333"));
        assertThat(verbruikKostenOverzicht.getStroomKostenNormaal()).isEqualTo(new BigDecimal("99.990"));
    }

    @Test
    public void whenGetVerbruikPerDagForCurrentDayThenUsageAreRetrievedFromNonCachedService() {
        timeTravelTo(clock, LocalDate.of(2016, JANUARY, 4).atTime(14, 43, 13));

        VerbruikKostenOverzichtService verbruikKostenOverzichtServiceProxyWithEnabledCaching = mock(VerbruikKostenOverzichtService.class);
        setField(verbruikKostenOverzichtServiceProxyWithEnabledCaching, "verbruikKostenOverzichtServiceProxyWithEnabledCaching", verbruikKostenOverzichtServiceProxyWithEnabledCaching);

        LocalDate day = LocalDate.of(2016, JANUARY, 4);
        DateTimePeriod period = aPeriodWithToDateTime(day.atStartOfDay(), day.plusDays(1).atStartOfDay());

        Energiecontract energiecontract = new Energiecontract();
        energiecontract.setVan(0L);
        energiecontract.setTotEnMet(Long.MAX_VALUE);
        energiecontract.setGasPerKuub(new BigDecimal("10"));
        energiecontract.setStroomPerKwhDalTarief(new BigDecimal("20"));
        energiecontract.setStroomPerKwhNormaalTarief(new BigDecimal("30"));
        when(energiecontractService.findAllInInPeriod(any())).thenReturn(singletonList(energiecontract));

        when(verbruikRepository.getGasVerbruikInPeriod(toMillisSinceEpochAtStartOfDay(day) + MILLIS_PER_HOUR, toMillisSinceEpochAtStartOfDay(day.plusDays(1)) - 1 + MILLIS_PER_HOUR))
                .thenReturn(new BigDecimal("1.000"));
        when(verbruikRepository.getStroomVerbruikDalTariefInPeriod(toMillisSinceEpochAtStartOfDay(day), toMillisSinceEpochAtStartOfDay(day.plusDays(1)) - 1))
                .thenReturn(new BigDecimal("2.000"));
        when(verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(toMillisSinceEpochAtStartOfDay(day), toMillisSinceEpochAtStartOfDay(day.plusDays(1)) - 1))
                .thenReturn(new BigDecimal("3.000"));

        VerbruikKostenOverzicht verbruikKostenOverzicht = verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period);

        assertThat(verbruikKostenOverzicht.getGasVerbruik()).isEqualTo(new BigDecimal("1.000"));
        assertThat(verbruikKostenOverzicht.getGasKosten()).isEqualTo(new BigDecimal("10.000"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.000"));
        assertThat(verbruikKostenOverzicht.getStroomKostenDal()).isEqualTo(new BigDecimal("40.000"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.000"));
        assertThat(verbruikKostenOverzicht.getStroomKostenNormaal()).isEqualTo(new BigDecimal("90.000"));

        verifyZeroInteractions(verbruikKostenOverzichtServiceProxyWithEnabledCaching);
    }

    @Test
    public void whenGetAveragesThenAveragesAreReturned() {
        VerbruikKostenOverzicht verbruikKostenOverzicht1 = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht1.setGasVerbruik(new BigDecimal(42.023));
        verbruikKostenOverzicht1.setGasKosten(new BigDecimal(10.000));
        verbruikKostenOverzicht1.setStroomVerbruikDal(new BigDecimal(123.000));
        verbruikKostenOverzicht1.setStroomKostenDal(new BigDecimal(12.872));
        verbruikKostenOverzicht1.setStroomVerbruikNormaal(new BigDecimal(2450.607));
        verbruikKostenOverzicht1.setStroomKostenNormaal(new BigDecimal(2312.023));

        VerbruikKostenOverzicht verbruikKostenOverzicht2 = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht2.setGasVerbruik(new BigDecimal(21.531));
        verbruikKostenOverzicht2.setGasKosten(new BigDecimal(34.131));
        verbruikKostenOverzicht2.setStroomVerbruikDal(new BigDecimal(134.012));
        verbruikKostenOverzicht2.setStroomKostenDal(new BigDecimal(71.325));
        verbruikKostenOverzicht2.setStroomVerbruikNormaal(new BigDecimal(2321.242));
        verbruikKostenOverzicht2.setStroomKostenNormaal(new BigDecimal(9214.081));

        VerbruikKostenOverzicht gemiddeldeVerbruikPerDagInPeriode = verbruikKostenOverzichtService.getAverages(asList(verbruikKostenOverzicht1, verbruikKostenOverzicht2));

        assertThat(gemiddeldeVerbruikPerDagInPeriode.getGasVerbruik()).isEqualTo(new BigDecimal("31.777"));
        assertThat(gemiddeldeVerbruikPerDagInPeriode.getStroomVerbruikDal()).isEqualTo(new BigDecimal("128.506"));
        assertThat(gemiddeldeVerbruikPerDagInPeriode.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("2385.925"));

        assertThat(gemiddeldeVerbruikPerDagInPeriode.getGasKosten()).isEqualTo(new BigDecimal("22.07"));
        assertThat(gemiddeldeVerbruikPerDagInPeriode.getStroomKostenDal()).isEqualTo(new BigDecimal("42.10"));
        assertThat(gemiddeldeVerbruikPerDagInPeriode.getStroomKostenNormaal()).isEqualTo(new BigDecimal("5763.05"));
    }
}