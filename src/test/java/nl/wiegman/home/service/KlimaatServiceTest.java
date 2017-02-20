package nl.wiegman.home.service;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.model.KlimaatSensor;
import nl.wiegman.home.repository.KlimaatRepos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class KlimaatServiceTest {

    @Mock
    private KlimaatRepos klimaatRepositoryMock;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private KlimaatService klimaatService;

    @Captor
    private ArgumentCaptor<Klimaat> klimaatArgumentCaptor;

    @Test
    public void testHappyFlow() {
        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode("LIVINGROOM");

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

        verify(klimaatRepositoryMock, times(1)).save(klimaatArgumentCaptor.capture());

        Klimaat savedKlimaat = klimaatArgumentCaptor.getValue();
        assertThat(savedKlimaat.getTemperatuur().doubleValue()).isEqualTo(21.0d);
        assertThat(savedKlimaat.getLuchtvochtigheid().doubleValue()).isEqualTo(51.0d);
        assertThat(savedKlimaat.getKlimaatSensor()).isEqualTo(klimaatSensor);
    }
}