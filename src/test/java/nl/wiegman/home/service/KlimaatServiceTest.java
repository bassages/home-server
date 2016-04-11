package nl.wiegman.home.service;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.repository.KlimaatRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class KlimaatServiceTest {

    @Mock
    private KlimaatRepo klimaatRepositoryMock;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private KlimaatService klimaatService;

    @Captor
    private ArgumentCaptor<Klimaat> klimaatArgumentCaptor;

    @Test
    public void testHappyFlow() {
        Klimaat klimaat = new Klimaat();
        klimaat.setTemperatuur(new BigDecimal(20.0));
        klimaat.setLuchtvochtigheid(new BigDecimal(50.0));
        klimaatService.add(klimaat);

        klimaat = new Klimaat();
        klimaat.setTemperatuur(new BigDecimal(21.0));
        klimaat.setLuchtvochtigheid(new BigDecimal(51.0));
        klimaatService.add(klimaat);

        klimaat = new Klimaat();
        klimaat.setTemperatuur(new BigDecimal(22.0));
        klimaat.setLuchtvochtigheid(new BigDecimal(52.0));
        klimaatService.add(klimaat);

        klimaatService.save();

        verify(klimaatRepositoryMock, times(1)).save(klimaatArgumentCaptor.capture());

        assertThat(klimaatArgumentCaptor.getValue().getTemperatuur().doubleValue()).isEqualTo(21.0d);
        assertThat(klimaatArgumentCaptor.getValue().getLuchtvochtigheid().doubleValue()).isEqualTo(51.0d);
    }
}