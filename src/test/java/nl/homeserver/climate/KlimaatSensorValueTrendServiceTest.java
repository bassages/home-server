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
import static nl.homeserver.climate.KlimaatBuilder.aKlimaat;
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
                aKlimaat().withDatumtijd(day.atTime(14, 0, 0)).withTemperatuur(new BigDecimal("19.00")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 4, 0)).withTemperatuur(new BigDecimal("20.20")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 8, 0)).withTemperatuur(new BigDecimal("21.70")).withKlimaatSensor(klimaatSensor).build()
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
                aKlimaat().withDatumtijd(day.atTime(14, 0, 0)).withTemperatuur(new BigDecimal("21.70")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 4, 0)).withTemperatuur(new BigDecimal("20.20")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 8, 0)).withTemperatuur(new BigDecimal("19.00")).withKlimaatSensor(klimaatSensor).build()
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
                aKlimaat().withDatumtijd(day.atTime(14, 0, 0)).withTemperatuur(new BigDecimal("20.00")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 4, 0)).withTemperatuur(new BigDecimal("20.00")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 8, 0)).withTemperatuur(new BigDecimal("20.00")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 12, 0)).withTemperatuur(new BigDecimal("20.00")).withKlimaatSensor(klimaatSensor).build()
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
                aKlimaat().withDatumtijd(day.atTime(14, 0, 0)).withTemperatuur(invalidTemperatuur).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 4, 0)).withTemperatuur(invalidTemperatuur).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 8, 0)).withTemperatuur(invalidTemperatuur).withKlimaatSensor(klimaatSensor).build());

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
