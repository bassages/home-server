package nl.wiegman.home.klimaat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RunWith(MockitoJUnitRunner.class)
public class KlimaatServiceTest {

    @Mock
    private KlimaatRepos klimaatRepository;
    @Mock
    private KlimaatSensorRepository klimaatSensorRepository;
    @Mock
    private KlimaatSensorValueTrendService klimaatSensorValueTrendService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private KlimaatService klimaatService;

    @Captor
    private ArgumentCaptor<Klimaat> klimaatArgumentCaptor;

    @Test
    public void testHappyFlow() {
        String klimaatSensorCode = "LIVINGROOM";
        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode(klimaatSensorCode);

        when(klimaatSensorRepository.findFirstByCode(klimaatSensorCode)).thenReturn(klimaatSensor);

        Klimaat klimaat = new Klimaat();
        klimaat.setTemperatuur(new BigDecimal(20.0));
        klimaat.setLuchtvochtigheid(new BigDecimal(50.0));
        klimaat.setKlimaatSensor(klimaatSensor);
        klimaatService.add(klimaat);

        klimaat = new Klimaat();
        klimaat.setTemperatuur(new BigDecimal(21.0));
        klimaat.setLuchtvochtigheid(new BigDecimal(51.0));
        klimaat.setKlimaatSensor(klimaatSensor);
        klimaatService.add(klimaat);

        klimaat = new Klimaat();
        klimaat.setTemperatuur(new BigDecimal(22.0));
        klimaat.setLuchtvochtigheid(new BigDecimal(52.0));
        klimaat.setKlimaatSensor(klimaatSensor);
        klimaatService.add(klimaat);

        klimaatService.save();

        verify(klimaatRepository, times(1)).save(klimaatArgumentCaptor.capture());

        Klimaat savedKlimaat = klimaatArgumentCaptor.getValue();
        assertThat(savedKlimaat.getTemperatuur().doubleValue()).isEqualTo(21.0d);
        assertThat(savedKlimaat.getLuchtvochtigheid().doubleValue()).isEqualTo(51.0d);
        assertThat(savedKlimaat.getKlimaatSensor()).isEqualTo(klimaatSensor);
    }
}