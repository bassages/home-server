package nl.homeserver.energie.verbruikkosten;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import nl.homeserver.energie.energycontract.EnergyContractService;
import nl.homeserver.energie.energycontract.EnergyContract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.JANUARY;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerbruikKostenInPeriodServiceTest {

    @InjectMocks
    VerbruikKostenInPeriodService verbruikKostenInPeriodService;

    @Mock
    EnergyContractService energyContractService;
    @Mock
    VerbruikKostenService verbruikKostenService;

    @Mock
    VerbruikProvider verbruikProvider;

    @Test
    void whenNotCachedGetGasVerbruikInPeriodeThenEnergyContractsInPeriodIsConsidered() {
        // given
        final LocalDateTime from = LocalDate.of(2016, JANUARY, 2).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2016, JANUARY, 4).atStartOfDay();
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final EnergyContract energyContract1 = new EnergyContract();
        final EnergyContract energyContract2 = new EnergyContract();
        when(energyContractService.findAllInInPeriod(period)).thenReturn(List.of(energyContract1, energyContract2));

        when(verbruikKostenService.getGasVerbruikKosten(verbruikProvider, energyContract1, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("1"), new BigDecimal("2.001")));
        when(verbruikKostenService.getGasVerbruikKosten(verbruikProvider, energyContract2, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("7"), new BigDecimal("4.123")));

        // when
        final VerbruikKosten gasVerbruikInPeriode = verbruikKostenInPeriodService
                .getNotCachedGasVerbruikInPeriode(verbruikProvider, period);

        // then
        assertThat(gasVerbruikInPeriode.getVerbruik()).isEqualTo(new BigDecimal("8"));
        assertThat(gasVerbruikInPeriode.getKosten()).isEqualTo(new BigDecimal("6.124"));
    }

    @Test
    void whenGetPotentiallyCachedGasVerbruikInPeriodeThenEnergyContractsInPeriodIsConsidered() {
        // given
        final LocalDateTime from = LocalDate.of(2016, JANUARY, 2).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2016, JANUARY, 4).atStartOfDay();
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final EnergyContract energyContract1 = new EnergyContract();
        final EnergyContract energyContract2 = new EnergyContract();
        when(energyContractService.findAllInInPeriod(period)).thenReturn(List.of(energyContract1, energyContract2));

        when(verbruikKostenService.getGasVerbruikKosten(verbruikProvider, energyContract1, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("1"), new BigDecimal("2.001")));
        when(verbruikKostenService.getGasVerbruikKosten(verbruikProvider, energyContract2, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("7"), new BigDecimal("4.123")));

        // when
        final VerbruikKosten gasVerbruikInPeriode = verbruikKostenInPeriodService
                .getPotentiallyCachedGasVerbruikInPeriode(verbruikProvider, period);

        // then
        assertThat(gasVerbruikInPeriode.getVerbruik()).isEqualTo(new BigDecimal("8"));
        assertThat(gasVerbruikInPeriode.getKosten()).isEqualTo(new BigDecimal("6.124"));
    }

    @Test
    void whenGetNotCachedStroomVerbruikInPeriodeThenEnergyContractsInPeriodIsConsidered() {
        // given
        final LocalDateTime from = LocalDate.of(2016, JANUARY, 2).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2016, JANUARY, 4).atStartOfDay();
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final EnergyContract energyContract1 = new EnergyContract();
        final EnergyContract energyContract2 = new EnergyContract();
        when(energyContractService.findAllInInPeriod(period)).thenReturn(List.of(energyContract1, energyContract2));

        final StroomTariefIndicator stroomTariefIndicator = NORMAAL;
        when(verbruikKostenService.getStroomVerbruikKosten(verbruikProvider, energyContract1, stroomTariefIndicator, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("100"), new BigDecimal("2111.123")));
        when(verbruikKostenService.getStroomVerbruikKosten(verbruikProvider, energyContract2, stroomTariefIndicator, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("124"), new BigDecimal("1000.123")));

        // when
        final VerbruikKosten gasVerbruikInPeriode = verbruikKostenInPeriodService
                .getNotCachedStroomVerbruikInPeriode(verbruikProvider, period, stroomTariefIndicator);

        // then
        assertThat(gasVerbruikInPeriode.getVerbruik()).isEqualTo(new BigDecimal("224"));
        assertThat(gasVerbruikInPeriode.getKosten()).isEqualTo(new BigDecimal("3111.246"));
    }

    @Test
    void whenGetPotentiallyCachedStroomVerbruikInPeriodeThenEnergyContractsInPeriodIsConsidered() {
        // given
        final LocalDateTime from = LocalDate.of(2016, JANUARY, 2).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2016, JANUARY, 4).atStartOfDay();
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final EnergyContract energyContract1 = new EnergyContract();
        final EnergyContract energyContract2 = new EnergyContract();
        when(energyContractService.findAllInInPeriod(period)).thenReturn(List.of(energyContract1, energyContract2));

        final StroomTariefIndicator stroomTariefIndicator = NORMAAL;
        when(verbruikKostenService.getStroomVerbruikKosten(verbruikProvider, energyContract1, stroomTariefIndicator, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("100"), new BigDecimal("2111.123")));
        when(verbruikKostenService.getStroomVerbruikKosten(verbruikProvider, energyContract2, stroomTariefIndicator, period))
                .thenReturn(new VerbruikKosten(new BigDecimal("124"), new BigDecimal("1000.123")));

        // when
        final VerbruikKosten gasVerbruikInPeriode = verbruikKostenInPeriodService
                .getPotentiallyCachedStroomVerbruikInPeriode(verbruikProvider, period, stroomTariefIndicator);

        // then
        assertThat(gasVerbruikInPeriode.getVerbruik()).isEqualTo(new BigDecimal("224"));
        assertThat(gasVerbruikInPeriode.getKosten()).isEqualTo(new BigDecimal("3111.246"));
    }
}
