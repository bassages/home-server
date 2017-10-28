package nl.wiegman.home.klimaat;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
                getKlimaat(toDate("01-01-2016 14:00:00"), new BigDecimal("19.00")),
                getKlimaat(toDate("01-01-2016 14:04:00"), new BigDecimal("20.20")),
                getKlimaat(toDate("01-01-2016 14:08:00"), new BigDecimal("21.70"))
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.UP);
    }

    @Test
    public void whenTemperatureDropsThenTrendIsDown() throws Exception {
        List<Klimaat> klimaats = asList(
            getKlimaat(toDate("01-01-2016 14:00:00"), new BigDecimal("21.70")),
            getKlimaat(toDate("01-01-2016 14:04:00"), new BigDecimal("20.20")),
            getKlimaat(toDate("01-01-2016 14:08:00"), new BigDecimal("19.00"))
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.DOWN);
    }

    @Test
    public void whenTemperatureIsSameThenTrendIsStable() throws Exception {
        List<Klimaat> klimaats = asList(
            getKlimaat(toDate("01-01-2016 14:00:00"), new BigDecimal("20.00")),
            getKlimaat(toDate("01-01-2016 14:04:00"), new BigDecimal("20.00")),
            getKlimaat(toDate("01-01-2016 14:08:00"), new BigDecimal("20.00"))
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isEqualTo(Trend.STABLE);
    }

    @Test
    public void whenNotEnoughValidSamplesThenTrendIsUndetermined() throws Exception {
        List<Klimaat> klimaats = asList(
                getKlimaat(toDate("01-01-2016 14:00:00"), null),
                getKlimaat(toDate("01-01-2016 14:04:00"), null),
                getKlimaat(toDate("01-01-2016 14:08:00"), null)
        );

        assertThat(klimaatSensorValueTrendService.determineValueTrend(klimaats, Klimaat::getTemperatuur)).isNull();
    }

    @Test
    public void whenNotEnoughSamplesThenTrendIsUndetermined() throws Exception {
        assertThat(klimaatSensorValueTrendService.determineValueTrend(emptyList(), Klimaat::getTemperatuur)).isNull();
    }

    private Klimaat getKlimaat(Date datumtijd, BigDecimal temperatuur) throws ParseException {
        Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(datumtijd);
        klimaat.setTemperatuur(temperatuur);
        klimaat.setKlimaatSensor(klimaatSensor);
        return klimaat;
    }

    private Date toDate(String dateString) throws ParseException {
        return DateUtils.parseDate(dateString, "dd-MM-yyyy HH:mm:ss");
    }
}