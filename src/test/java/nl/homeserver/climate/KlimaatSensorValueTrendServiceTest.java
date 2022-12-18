package nl.homeserver.climate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.time.Month.JULY;
import static java.util.Collections.emptyList;
import static nl.homeserver.climate.Klimaat.aKlimaat;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class KlimaatSensorValueTrendServiceTest {

    @InjectMocks
    KlimaatSensorValueTrendService klimaatSensorValueTrendService;

    @Mock
    KlimaatSensor klimaatSensor;

    @Test
    void givenTemperatureRisesWhenDetermineValueTrendThenTrendIsUp() {
        // given
        final LocalDate day = LocalDate.of(2016, JULY, 1);

        final List<Klimaat> klimaats = List.of(
                aKlimaat().datumtijd(day.atTime(14, 0, 0)).temperatuur(new BigDecimal("19.00")).klimaatSensor(klimaatSensor).build(),
                aKlimaat().datumtijd(day.atTime(14, 4, 0)).temperatuur(new BigDecimal("20.20")).klimaatSensor(klimaatSensor).build(),
                aKlimaat().datumtijd(day.atTime(14, 8, 0)).temperatuur(new BigDecimal("21.70")).klimaatSensor(klimaatSensor).build()
        );

        // when
        final Trend trend = klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur);

        // then
        assertThat(trend).isEqualTo(Trend.UP);
    }

    @Test
    void givenTemperatureDropsWhenDetermineValueTrendThenTrendIsDown() {
         //given
        final LocalDate day = LocalDate.of(2016, JULY, 1);

        final List<Klimaat> klimaats = List.of(
                aKlimaat().datumtijd(day.atTime(14, 0, 0)).temperatuur(new BigDecimal("21.70")).klimaatSensor(klimaatSensor).build(),
                aKlimaat().datumtijd(day.atTime(14, 4, 0)).temperatuur(new BigDecimal("20.20")).klimaatSensor(klimaatSensor).build(),
                aKlimaat().datumtijd(day.atTime(14, 8, 0)).temperatuur(new BigDecimal("19.00")).klimaatSensor(klimaatSensor).build()
        );

        // wehn
        final Trend trend = klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur);

        // then
        assertThat(trend).isEqualTo(Trend.DOWN);
    }

    @Test
    void givenTemperatureRemainsTheSameWhenDetermineValueTrendThenTrendIsStable() {
        // given
        final LocalDate day = LocalDate.of(2016, JULY, 1);

        final List<Klimaat> klimaats = List.of(
                aKlimaat().datumtijd(day.atTime(14, 0, 0)).temperatuur(new BigDecimal("20.00")).klimaatSensor(klimaatSensor).build(),
                aKlimaat().datumtijd(day.atTime(14, 4, 0)).temperatuur(new BigDecimal("20.00")).klimaatSensor(klimaatSensor).build(),
                aKlimaat().datumtijd(day.atTime(14, 8, 0)).temperatuur(new BigDecimal("20.00")).klimaatSensor(klimaatSensor).build(),
                aKlimaat().datumtijd(day.atTime(14, 12, 0)).temperatuur(new BigDecimal("20.00")).klimaatSensor(klimaatSensor).build()
        );

        // when
        final Trend trend = klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur);

        // then
        assertThat(trend).isEqualTo(Trend.STABLE);
    }

    @Test
    void givenNotEnoughValidSamplesWhenDetermineValueTrendThenTrendIsUndetermined() {
        // given
        final LocalDate day = LocalDate.of(2016, JULY, 1);
        final BigDecimal invalidTemperatuur = null;

        final List<Klimaat> klimaats = List.of(
                aKlimaat().datumtijd(day.atTime(14, 0, 0)).temperatuur(invalidTemperatuur).klimaatSensor(klimaatSensor).build(),
                aKlimaat().datumtijd(day.atTime(14, 4, 0)).temperatuur(invalidTemperatuur).klimaatSensor(klimaatSensor).build(),
                aKlimaat().datumtijd(day.atTime(14, 8, 0)).temperatuur(invalidTemperatuur).klimaatSensor(klimaatSensor).build());

        // when
        final Trend trend = klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur);

        // then
        assertThat(trend).isEqualTo(Trend.UNKNOWN);
    }

    @Test
    void givenNotEnoughSamplesWhenDetermineValueTrendThenTrendIsUndetermined() {
        // when
        final Trend trend = klimaatSensorValueTrendService.determineValueTrend(emptyList(), Klimaat::getTemperatuur);

        // then
        assertThat(trend).isEqualTo(Trend.UNKNOWN);
    }
}
