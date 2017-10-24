package nl.wiegman.home.klimaat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import nl.wiegman.home.UpdateEvent;

@RunWith(MockitoJUnitRunner.class)
public class RealtimeKlimaatControllerTest {

    @InjectMocks
    private RealtimeKlimaatController realtimeKlimaatController;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private UpdateEvent updateEvent;
    @Mock
    private KlimaatSensor klimaatSensor;

    @Captor
    private ArgumentCaptor<RealtimeKlimaat> realtimeKlimaatArgumentCaptor;

    @Before
    public void setup() {
        when(klimaatSensor.getCode()).thenReturn("LIVINGROOM");
    }

    @Test
    public void whenTemperatureRisesThenTrendIsUp() throws Exception {
        generateUpdateEvent(toDate("01-01-2016 14:00:00"), new BigDecimal("19.00"), updateEvent, simpMessagingTemplate);
        generateUpdateEvent(toDate("01-01-2016 14:04:00"), new BigDecimal("20.20"), updateEvent, simpMessagingTemplate);
        generateUpdateEvent(toDate("01-01-2016 14:08:00"), new BigDecimal("21.70"), updateEvent, simpMessagingTemplate);

        List<RealtimeKlimaat> realTimeKimaatValues = realtimeKlimaatArgumentCaptor.getAllValues();
        assertThat(realTimeKimaatValues).hasSize(3);

        assertThat(realTimeKimaatValues.get(0).getTemperatuurTrend()).isNull();
        assertThat(realTimeKimaatValues.get(1).getTemperatuurTrend()).isNull();
        assertThat(realTimeKimaatValues.get(2).getTemperatuurTrend()).isEqualTo(Trend.UP);
    }

    @Test
    public void whenTemperatureDropsThenTrendIsDown() throws Exception {
        generateUpdateEvent(toDate("01-01-2016 14:00:00"), new BigDecimal("21.70"), updateEvent, simpMessagingTemplate);
        generateUpdateEvent(toDate("01-01-2016 14:04:00"), new BigDecimal("20.20"), updateEvent, simpMessagingTemplate);
        generateUpdateEvent(toDate("01-01-2016 14:08:00"), new BigDecimal("19.00"), updateEvent, simpMessagingTemplate);

        List<RealtimeKlimaat> realTimeKimaatValues = realtimeKlimaatArgumentCaptor.getAllValues();
        assertThat(realTimeKimaatValues).hasSize(3);

        assertThat(realTimeKimaatValues.get(0).getTemperatuurTrend()).isNull();
        assertThat(realTimeKimaatValues.get(1).getTemperatuurTrend()).isNull();
        assertThat(realTimeKimaatValues.get(2).getTemperatuurTrend()).isEqualTo(Trend.DOWN);
    }

    @Test
    public void whenTemperatureIsSameThenTrendIsStable() throws Exception {
        generateUpdateEvent(toDate("01-01-2016 14:00:00"), new BigDecimal("20.00"), updateEvent, simpMessagingTemplate);
        generateUpdateEvent(toDate("01-01-2016 14:04:00"), new BigDecimal("20.00"), updateEvent, simpMessagingTemplate);
        generateUpdateEvent(toDate("01-01-2016 14:08:00"), new BigDecimal("20.00"), updateEvent, simpMessagingTemplate);

        List<RealtimeKlimaat> realTimeKimaatValues = realtimeKlimaatArgumentCaptor.getAllValues();
        assertThat(realTimeKimaatValues).hasSize(3);

        assertThat(realTimeKimaatValues.get(0).getTemperatuurTrend()).isNull();
        assertThat(realTimeKimaatValues.get(1).getTemperatuurTrend()).isNull();
        assertThat(realTimeKimaatValues.get(2).getTemperatuurTrend()).isEqualTo(Trend.STABLE);
    }

    private void generateUpdateEvent(Date datumtijd, BigDecimal temperatuur, UpdateEvent updateEvent, SimpMessagingTemplate simpMessagingTemplate)
            throws ParseException {

        Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(datumtijd);
        klimaat.setTemperatuur(temperatuur);
        klimaat.setKlimaatSensor(klimaatSensor);

        when(updateEvent.getUpdatedObject()).thenReturn(klimaat);

        realtimeKlimaatController.onApplicationEvent(updateEvent);

        verify(simpMessagingTemplate).convertAndSend(anyString(), realtimeKlimaatArgumentCaptor.capture());
        reset(simpMessagingTemplate);
    }

    private Date toDate(String dateString) throws ParseException {
        return DateUtils.parseDate(dateString, "dd-MM-yyyy HH:mm:ss");
    }
}