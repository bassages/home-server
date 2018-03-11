package nl.homeserver.klimaat;

import static java.time.Month.JULY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.homeserver.Trend;

@RunWith(MockitoJUnitRunner.class)
public class KlimaatSensorValueTrendServiceTest {

    @InjectMocks
    private KlimaatSensorValueTrendService klimaatSensorValueTrendService;

    @Mock
    private KlimaatSensor klimaatSensor;

    @Before
    public void setup() {
        when(klimaatSensor.getCode()).thenReturn("LIVINGROOM");
    }

    @Test
    public void givenTemperatureRisesWhenDetermineValueTrendThenTrendIsUp() throws Exception {
        LocalDate day = LocalDate.of(2016, JULY, 1);

        List<Klimaat> klimaats = asList(
                aKlimaat().withDatumtijd(day.atTime(14, 0, 0)).withTemperatuur(new BigDecimal("19.00")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 4, 0)).withTemperatuur(new BigDecimal("20.20")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 8, 0)).withTemperatuur(new BigDecimal("21.70")).withKlimaatSensor(klimaatSensor).build()
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.UP);
    }

    @Test
    public void givenTemperatureDropsWhenDetermineValueTrendThenTrendIsDown() {
        LocalDate day = LocalDate.of(2016, JULY, 1);

        List<Klimaat> klimaats = asList(
                aKlimaat().withDatumtijd(day.atTime(14, 0, 0)).withTemperatuur(new BigDecimal("21.70")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 4, 0)).withTemperatuur(new BigDecimal("20.20")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 8, 0)).withTemperatuur(new BigDecimal("19.00")).withKlimaatSensor(klimaatSensor).build()
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.DOWN);
    }

    @Test
    public void givenTemperatureRemainsTheSameWhenDetermineValueTrendThenTrendIsStable() {
        LocalDate day = LocalDate.of(2016, JULY, 1);

        List<Klimaat> klimaats = asList(
                aKlimaat().withDatumtijd(day.atTime(14, 0, 0)).withTemperatuur(new BigDecimal("20.00")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 4, 0)).withTemperatuur(new BigDecimal("20.00")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 8, 0)).withTemperatuur(new BigDecimal("20.00")).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 12, 0)).withTemperatuur(new BigDecimal("20.00")).withKlimaatSensor(klimaatSensor).build()
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.STABLE);
    }

    @Test
    public void givenNotEnoughValidSamplesWhenDetermineValueTrendThenTrendIsUndetermined() {
        LocalDate day = LocalDate.of(2016, JULY, 1);

        BigDecimal invalidTemperatuur = null;

        List<Klimaat> klimaats = asList(
                aKlimaat().withDatumtijd(day.atTime(14, 0, 0)).withTemperatuur(invalidTemperatuur).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 4, 0)).withTemperatuur(invalidTemperatuur).withKlimaatSensor(klimaatSensor).build(),
                aKlimaat().withDatumtijd(day.atTime(14, 8, 0)).withTemperatuur(invalidTemperatuur).withKlimaatSensor(klimaatSensor).build()
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.UNKNOWN);
    }

    @Test
    public void givenNotEnoughSamplesWhenDetermineValueTrendThenTrendIsUndetermined() {
        assertThat(klimaatSensorValueTrendService.determineValueTrend(emptyList(), Klimaat::getTemperatuur)).isEqualTo(Trend.UNKNOWN);
    }
}