package nl.homeserver.energie.verbruikkosten;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.energycontract.Energycontract;
import nl.homeserver.energie.energycontract.EnergyContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.JANUARY;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.energie.StroomTariefIndicator.DAL;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class VerbruikKostenOverzichtServiceTest {

    @InjectMocks
    VerbruikKostenOverzichtService verbruikKostenOverzichtService;

    @Mock
    VerbruikProvider verbruikProvider;
    @Mock
    EnergyContractService energycontractService;
    @Mock
    Clock clock;

    @BeforeEach
    void setup() {
        setField(verbruikKostenOverzichtService, "verbruikKostenOverzichtServiceProxyWithEnabledCaching", verbruikKostenOverzichtService);
    }

    @Test
    void whenGetForPeriodInFutureThenNoOtherServicesCalledAndUsageIsZero() {
        timeTravelTo(clock, LocalDate.of(2016, JANUARY, 1).atStartOfDay());

        final LocalDateTime from = LocalDateTime.of(2016, JANUARY, 2, 10, 6);
        final LocalDateTime to = LocalDateTime.of(2016, JANUARY, 3, 4, 13);
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final VerbruikKostenOverzicht verbruikKostenOverzicht = verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(verbruikProvider, period);

        assertThat(verbruikKostenOverzicht.getGasKosten()).isNull();
        assertThat(verbruikKostenOverzicht.getGasVerbruik()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomKostenDal()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomVerbruikDal()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomKostenNormaal()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomVerbruikNormaal()).isNull();

        verifyNoMoreInteractions(verbruikProvider, energycontractService);
    }

    @Test
    void givenMultipleEnergycontractsWhenGetForPeriodInThePastThenUsagesAndCostsAreRetrievedFromCacheAndCostsAreCalculatedBasedOnValidEnergycontract() {
        timeTravelTo(clock, LocalDate.of(2016, JANUARY, 4).atStartOfDay());

        final LocalDateTime from = LocalDate.of(2016, JANUARY, 2).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2016, JANUARY, 4).atStartOfDay();
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final Energycontract energycontract1 = new Energycontract();
        energycontract1.setValidFrom(LocalDate.of(2000, JANUARY, 1));
        energycontract1.setValidTo(from.plusDays(1).toLocalDate());
        energycontract1.setGasPerKuub(new BigDecimal("10"));
        energycontract1.setStroomPerKwhDalTarief(new BigDecimal("20"));
        energycontract1.setStroomPerKwhNormaalTarief(new BigDecimal("30"));

        final Energycontract energycontract2 = new Energycontract();
        energycontract2.setValidFrom(energycontract1.getValidTo());
        energycontract2.setValidTo(null);
        energycontract2.setGasPerKuub(new BigDecimal("40"));
        energycontract2.setStroomPerKwhDalTarief(new BigDecimal("50"));
        energycontract2.setStroomPerKwhNormaalTarief(new BigDecimal("60"));

        when(energycontractService.findAllInInPeriod(period)).thenReturn(List.of(energycontract1, energycontract2));

        lenient().when(verbruikProvider.getGasVerbruik(aPeriodWithToDateTime(from, from.plusDays(1)))).thenReturn(new BigDecimal("1.111"));
        lenient().when(verbruikProvider.getStroomVerbruik(aPeriodWithToDateTime(from, from.plusDays(1)), DAL)).thenReturn(new BigDecimal("2.222"));
        lenient().when(verbruikProvider.getStroomVerbruik(aPeriodWithToDateTime(from, from.plusDays(1)), NORMAAL)).thenReturn(new BigDecimal("3.333"));

        final VerbruikKostenOverzicht verbruikKostenOverzicht = verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(verbruikProvider, period);

        assertThat(verbruikKostenOverzicht.getGasVerbruik()).isEqualTo(new BigDecimal("1.111"));
        assertThat(verbruikKostenOverzicht.getGasKosten()).isEqualTo(new BigDecimal("11.110"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.222"));
        assertThat(verbruikKostenOverzicht.getStroomKostenDal()).isEqualTo(new BigDecimal("44.440"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.333"));
        assertThat(verbruikKostenOverzicht.getStroomKostenNormaal()).isEqualTo(new BigDecimal("99.990"));
    }

    @Test
    void whenGetVerbruikPerDagForCurrentDayThenUsageAreRetrievedFromNonCachedService() {
        timeTravelTo(clock, LocalDate.of(2016, JANUARY, 4).atTime(14, 43, 13));

        final VerbruikKostenOverzichtService verbruikKostenOverzichtServiceProxyWithEnabledCaching = mock(VerbruikKostenOverzichtService.class);
        setField(verbruikKostenOverzichtServiceProxyWithEnabledCaching, "verbruikKostenOverzichtServiceProxyWithEnabledCaching", verbruikKostenOverzichtServiceProxyWithEnabledCaching);

        final LocalDate day = LocalDate.of(2016, JANUARY, 4);
        final DateTimePeriod period = aPeriodWithToDateTime(day.atStartOfDay(), day.plusDays(1).atStartOfDay());

        final Energycontract energycontract = new Energycontract();
        energycontract.setValidFrom(LocalDate.of(2000, JANUARY, 1));
        energycontract.setValidTo(null);
        energycontract.setGasPerKuub(new BigDecimal("10"));
        energycontract.setStroomPerKwhDalTarief(new BigDecimal("20"));
        energycontract.setStroomPerKwhNormaalTarief(new BigDecimal("30"));
        when(energycontractService.findAllInInPeriod(period)).thenReturn(List.of(energycontract));

        when(verbruikProvider.getGasVerbruik(aPeriodWithToDateTime(day.atStartOfDay(), day.plusDays(1).atStartOfDay()))).thenReturn(new BigDecimal("1.000"));
        when(verbruikProvider.getStroomVerbruik(aPeriodWithToDateTime(day.atStartOfDay(), day.plusDays(1).atStartOfDay()), DAL)).thenReturn(new BigDecimal("2.000"));
        when(verbruikProvider.getStroomVerbruik(aPeriodWithToDateTime(day.atStartOfDay(), day.plusDays(1).atStartOfDay()), NORMAAL)).thenReturn(new BigDecimal("3.000"));

        final VerbruikKostenOverzicht verbruikKostenOverzicht = verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(verbruikProvider, period);

        assertThat(verbruikKostenOverzicht.getGasVerbruik()).isEqualTo(new BigDecimal("1.000"));
        assertThat(verbruikKostenOverzicht.getGasKosten()).isEqualTo(new BigDecimal("10.000"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.000"));
        assertThat(verbruikKostenOverzicht.getStroomKostenDal()).isEqualTo(new BigDecimal("40.000"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.000"));
        assertThat(verbruikKostenOverzicht.getStroomKostenNormaal()).isEqualTo(new BigDecimal("90.000"));

        verifyNoMoreInteractions(verbruikKostenOverzichtServiceProxyWithEnabledCaching);
    }
}
