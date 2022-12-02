package nl.homeserver.energy.verbruikkosten;

import nl.homeserver.DateTimePeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.Month.JANUARY;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.energy.StroomTariefIndicator.DAL;
import static nl.homeserver.energy.StroomTariefIndicator.NORMAAL;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerbruikKostenOverzichtServiceTest {

    @InjectMocks
    VerbruikKostenOverzichtService verbruikKostenOverzichtService;

    @Mock
    Clock clock;
    @Mock
    VerbruikProvider verbruikProvider;
    @Mock
    VerbruikKostenInPeriodService verbruikKostenInPeriodService;

    @Test
    void whenGetForPeriodInFutureThenNoOtherServicesCalledAndUsageIsZero() {
        timeTravelTo(clock, LocalDate.of(2016, JANUARY, 1).atStartOfDay());

        final LocalDateTime from = LocalDateTime.of(2016, JANUARY, 2, 10, 6);
        final LocalDateTime to = LocalDateTime.of(2016, JANUARY, 3, 4, 13);
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final VerbruikKostenOverzicht verbruikKostenOverzicht = verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period, verbruikProvider);

        assertThat(verbruikKostenOverzicht.getGasKosten()).isNull();
        assertThat(verbruikKostenOverzicht.getGasVerbruik()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomKostenDal()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomVerbruikDal()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomKostenNormaal()).isNull();
        assertThat(verbruikKostenOverzicht.getStroomVerbruikNormaal()).isNull();

        verifyNoMoreInteractions(verbruikProvider, verbruikKostenInPeriodService);
    }

    @Test
    void whenGetVerbruikEnKostenOverzichtForPastPeriodThenPotentiallyCachedUsagesAreRetrieved() {
        timeTravelTo(clock, LocalDate.of(2016, JANUARY, 4).atStartOfDay());

        final LocalDateTime from = LocalDate.of(2016, JANUARY, 2).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2016, JANUARY, 4).atStartOfDay();
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        when(verbruikKostenInPeriodService.getPotentiallyCachedGasVerbruikInPeriode(verbruikProvider, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("1.111"), new BigDecimal("11.110")));
        when(verbruikKostenInPeriodService.getPotentiallyCachedStroomVerbruikInPeriode(verbruikProvider, period, DAL))
                .thenReturn(new VerbruikKosten(new BigDecimal("2.222"), new BigDecimal("44.440")));
        when(verbruikKostenInPeriodService.getPotentiallyCachedStroomVerbruikInPeriode(verbruikProvider, period, NORMAAL))
                .thenReturn(new VerbruikKosten(new BigDecimal("3.333"), new BigDecimal("99.990")));

        final VerbruikKostenOverzicht verbruikKostenOverzicht = verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period, verbruikProvider);

        assertThat(verbruikKostenOverzicht.getGasVerbruik()).isEqualTo(new BigDecimal("1.111"));
        assertThat(verbruikKostenOverzicht.getGasKosten()).isEqualTo(new BigDecimal("11.110"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.222"));
        assertThat(verbruikKostenOverzicht.getStroomKostenDal()).isEqualTo(new BigDecimal("44.440"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.333"));
        assertThat(verbruikKostenOverzicht.getStroomKostenNormaal()).isEqualTo(new BigDecimal("99.990"));
    }

    @Test
    void whenGetVerbruikPerDagForCurrentDayThenNotCachedUsagesAreRetrieved() {
        timeTravelTo(clock, LocalDate.of(2016, JANUARY, 4).atTime(14, 43, 13));

        final LocalDate day = LocalDate.of(2016, JANUARY, 4);
        final DateTimePeriod period = aPeriodWithToDateTime(day.atStartOfDay(), day.plusDays(1).atStartOfDay());

        when(verbruikKostenInPeriodService.getNotCachedGasVerbruikInPeriode(verbruikProvider, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("1.000"), new BigDecimal("10.000")));
        when(verbruikKostenInPeriodService.getNotCachedStroomVerbruikInPeriode(verbruikProvider, period, DAL))
                .thenReturn(new VerbruikKosten(new BigDecimal("2.000"), new BigDecimal("40.000")));
        when(verbruikKostenInPeriodService.getNotCachedStroomVerbruikInPeriode(verbruikProvider, period, NORMAAL))
                .thenReturn(new VerbruikKosten(new BigDecimal("3.000"), new BigDecimal("90.000")));

        final VerbruikKostenOverzicht verbruikKostenOverzicht = verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period, verbruikProvider);

        assertThat(verbruikKostenOverzicht.getGasVerbruik()).isEqualTo(new BigDecimal("1.000"));
        assertThat(verbruikKostenOverzicht.getGasKosten()).isEqualTo(new BigDecimal("10.000"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikDal()).isEqualTo(new BigDecimal("2.000"));
        assertThat(verbruikKostenOverzicht.getStroomKostenDal()).isEqualTo(new BigDecimal("40.000"));
        assertThat(verbruikKostenOverzicht.getStroomVerbruikNormaal()).isEqualTo(new BigDecimal("3.000"));
        assertThat(verbruikKostenOverzicht.getStroomKostenNormaal()).isEqualTo(new BigDecimal("90.000"));

        verifyNoMoreInteractions(verbruikKostenInPeriodService);
    }
}
