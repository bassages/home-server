package nl.wiegman.home.klimaat;

import static java.time.Month.JULY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.wiegman.home.Trend;

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
    public void whenTemperatureRisesThenTrendIsUp() throws Exception {
        List<Klimaat> klimaats = asList(
                createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 0, 0), new BigDecimal("19.00")),
                createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 4, 0), new BigDecimal("20.20")),
                createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 8, 0), new BigDecimal("21.70"))
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.UP);
    }

    @Test
    public void whenTemperatureDropsThenTrendIsDown() {
        List<Klimaat> klimaats = asList(
            createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 0, 0), new BigDecimal("21.70")),
            createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 4, 0), new BigDecimal("20.20")),
            createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 8, 0), new BigDecimal("19.00"))
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.DOWN);
    }

    @Test
    public void whenTemperatureIsSameThenTrendIsStable() {
        List<Klimaat> klimaats = asList(
            createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 0, 0), new BigDecimal("20.00")),
            createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 4, 0), new BigDecimal("20.00")),
            createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 8, 0), new BigDecimal("20.00"))
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.STABLE);
    }

    @Test
    public void whenNotEnoughValidSamplesThenTrendIsUndetermined() {
        List<Klimaat> klimaats = asList(
                createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 0, 0), null),
                createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 4, 0), null),
                createKlimaat(LocalDate.of(2016, JULY, 1).atTime(14, 8, 0), null)
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isNull();
    }

    @Test
    public void whenNotEnoughSamplesThenTrendIsUndetermined() {
        assertThat(klimaatSensorValueTrendService.determineValueTrend(emptyList(), Klimaat::getTemperatuur)).isNull();
    }

    private Klimaat createKlimaat(LocalDateTime datumtijd, BigDecimal temperatuur) {
        Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(datumtijd);
        klimaat.setTemperatuur(temperatuur);
        klimaat.setKlimaatSensor(klimaatSensor);
        return klimaat;
    }
}