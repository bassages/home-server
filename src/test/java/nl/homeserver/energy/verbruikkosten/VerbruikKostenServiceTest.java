package nl.homeserver.energy.verbruikkosten;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energy.StroomTariefIndicator;
import nl.homeserver.energy.energycontract.EnergyContract;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Calendar.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerbruikKostenServiceTest {

    VerbruikKostenService verbruikKostenService = new VerbruikKostenService();

    @Test
    void givenThereIsGasVerbruikWhenGetGasVerbruikKostenBothKostenAndVerbruikAreNotNull() {
        // given
        final DateTimePeriod period = DateTimePeriod.of(LocalDate.of(2021, MAY, 12));

        final VerbruikProvider verbruikProvider = mock(VerbruikProvider.class);
        when(verbruikProvider.getGasVerbruik(period)).thenReturn(new BigDecimal("300"));

        final EnergyContract energyContract = EnergyContract.builder()
                .validFrom(period.getFromDateTime().toLocalDate())
                .validTo(period.getToDateTime().toLocalDate())
                .gasPerCubicMeter(new BigDecimal("0.02"))
                .build();

        // when
        final VerbruikKosten gasVerbruikKosten = verbruikKostenService.getGasVerbruikKosten(verbruikProvider, energyContract, period);

        // then
        assertThat(gasVerbruikKosten.getVerbruik()).isEqualTo(new BigDecimal("300"));
        assertThat(gasVerbruikKosten.getKosten()).isEqualTo(new BigDecimal("6.000"));
    }

    @Test
    void givenThereIsNoGasVerbruikWhenGetGasVerbruikKostenBothKostenAndVerbruikAreNull() {
        // given
        final DateTimePeriod period = DateTimePeriod.of(LocalDate.of(2021, MAY, 12));

        final VerbruikProvider verbruikProvider = mock(VerbruikProvider.class);
        when(verbruikProvider.getGasVerbruik(period)).thenReturn(null);

        final EnergyContract energyContract = EnergyContract.builder()
                .validFrom(period.getFromDateTime().toLocalDate())
                .validTo(period.getToDateTime().toLocalDate())
                .gasPerCubicMeter(new BigDecimal("0.02"))
                .build();

        // when
        final VerbruikKosten gasVerbruikKosten = verbruikKostenService.getGasVerbruikKosten(verbruikProvider, energyContract, period);

        // then
        assertThat(gasVerbruikKosten.getVerbruik()).isNull();
        assertThat(gasVerbruikKosten.getKosten()).isNull();
    }

    @Test
    void givenThereIsStroomVerbruikWhenGetStroomVerbruikKostenBothKostenAndVerbruikAreNotNull() {
        // given
        final DateTimePeriod period = DateTimePeriod.of(LocalDate.of(2021, MAY, 12));
        final StroomTariefIndicator stroomTariefIndicator = StroomTariefIndicator.NORMAAL;

        final VerbruikProvider verbruikProvider = mock(VerbruikProvider.class);

        when(verbruikProvider.getStroomVerbruik(period, stroomTariefIndicator)).thenReturn(new BigDecimal("300"));

        final EnergyContract energyContract = EnergyContract.builder()
                .validFrom(period.getFromDateTime().toLocalDate())
                .validTo(period.getToDateTime().toLocalDate())
                .electricityPerKwhStandardTariff(new BigDecimal("0.02"))
                .build();

        // when
        final VerbruikKosten stroomVerbruikKosten = verbruikKostenService.getStroomVerbruikKosten(
                verbruikProvider, energyContract, stroomTariefIndicator, period);

        // then
        assertThat(stroomVerbruikKosten.getVerbruik()).isEqualTo(new BigDecimal("300"));
        assertThat(stroomVerbruikKosten.getKosten()).isEqualTo(new BigDecimal("6.000"));
    }

    @Test
    void givenThereIsNoStroomVerbruikWhenGetStroomVerbruikKostenBothKostenAndVerbruikAreNull() {
        // given
        final DateTimePeriod period = DateTimePeriod.of(LocalDate.of(2021, MAY, 12));
        final StroomTariefIndicator stroomTariefIndicator = StroomTariefIndicator.NORMAAL;

        final VerbruikProvider verbruikProvider = mock(VerbruikProvider.class);
        when(verbruikProvider.getStroomVerbruik(period, stroomTariefIndicator)).thenReturn(null);

        final EnergyContract energyContract = EnergyContract.builder()
                .validFrom(period.getFromDateTime().toLocalDate())
                .validTo(period.getToDateTime().toLocalDate())
                .electricityPerKwhStandardTariff(new BigDecimal("0.02"))
                .build();

        // when
        final VerbruikKosten stroomVerbruikKosten = verbruikKostenService.getStroomVerbruikKosten(
                verbruikProvider, energyContract, stroomTariefIndicator, period);

        // then
        assertThat(stroomVerbruikKosten.getVerbruik()).isNull();
        assertThat(stroomVerbruikKosten.getKosten()).isNull();
    }

}
