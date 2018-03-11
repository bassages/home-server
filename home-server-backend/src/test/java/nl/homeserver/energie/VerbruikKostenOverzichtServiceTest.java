package nl.homeserver.energie;

import static java.time.LocalDateTime.now;
import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energiecontract.Energiecontract;
import nl.homeserver.energiecontract.EnergiecontractService;

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

        LocalDateTime from = LocalDate.of(2016, JANUARY, 2).atStartOfDay();
        LocalDateTime to = LocalDate.of(2016, JANUARY, 4).atStartOfDay();
        DateTimePeriod period = aPeriodWithToDateTime(from, to);

        Energiecontract energiecontract1 = new Energiecontract();
        energiecontract1.setValidFrom(LocalDate.of(2000, JANUARY, 1));
        energiecontract1.setValidTo(from.plusDays(1).toLocalDate());
        energiecontract1.setGasPerKuub(new BigDecimal("10"));
        energiecontract1.setStroomPerKwhDalTarief(new BigDecimal("20"));
        energiecontract1.setStroomPerKwhNormaalTarief(new BigDecimal("30"));

        Energiecontract energiecontract2 = new Energiecontract();
        energiecontract2.setValidFrom(energiecontract1.getValidTo());
        energiecontract2.setValidTo(null);
        energiecontract2.setGasPerKuub(new BigDecimal("40"));
        energiecontract2.setStroomPerKwhDalTarief(new BigDecimal("50"));
        energiecontract2.setStroomPerKwhNormaalTarief(new BigDecimal("60"));

        when(energiecontractService.findAllInInPeriod(period)).thenReturn(asList(energiecontract1, energiecontract2));

        when(verbruikRepository.getGasVerbruikInPeriod(from.plusHours(1), from.plusDays(1).plusHours(1))).thenReturn(new BigDecimal("1.111"));
        when(verbruikRepository.getStroomVerbruikDalTariefInPeriod(from, from.plusDays(1))).thenReturn(new BigDecimal("2.222"));
        when(verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(from, from.plusDays(1))).thenReturn(new BigDecimal("3.333"));

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
        energiecontract.setValidFrom(LocalDate.of(2000, JANUARY, 1));
        energiecontract.setValidTo(null);
        energiecontract.setGasPerKuub(new BigDecimal("10"));
        energiecontract.setStroomPerKwhDalTarief(new BigDecimal("20"));
        energiecontract.setStroomPerKwhNormaalTarief(new BigDecimal("30"));
        when(energiecontractService.findAllInInPeriod(period)).thenReturn(singletonList(energiecontract));

        when(verbruikRepository.getGasVerbruikInPeriod(day.atStartOfDay().plusHours(1), day.plusDays(1).atStartOfDay().plusHours(1))).thenReturn(new BigDecimal("1.000"));
        when(verbruikRepository.getStroomVerbruikDalTariefInPeriod(day.atStartOfDay(), day.plusDays(1).atStartOfDay())).thenReturn(new BigDecimal("2.000"));
        when(verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(day.atStartOfDay(), day.plusDays(1).atStartOfDay())).thenReturn(new BigDecimal("3.000"));

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
    public void whenGetPotentiallyCachedStroomVerbruikInPeriodeForUnknownTariefThenException() {
        Energiecontract energiecontract = new Energiecontract();
        energiecontract.setValidFrom(LocalDate.of(2018, JANUARY, 1));
        energiecontract.setValidTo(LocalDate.of(2019, JANUARY, 1));
        when(energiecontractService.findAllInInPeriod(any())).thenReturn(singletonList(energiecontract));

        StroomTariefIndicator unknownStroomTariefIndicator = null;

        DateTimePeriod period = aPeriodWithToDateTime(now(), now().plusDays(1));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> verbruikKostenOverzichtService.getPotentiallyCachedStroomVerbruikInPeriode(period, unknownStroomTariefIndicator))
                .withMessage("Unexpected StroomTariefIndicator: [null]");
    }
}