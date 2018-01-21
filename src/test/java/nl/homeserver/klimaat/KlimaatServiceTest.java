package nl.homeserver.klimaat;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import nl.homeserver.Trend;

@RunWith(MockitoJUnitRunner.class)
public class KlimaatServiceTest {

    @InjectMocks
    private KlimaatService klimaatService;

    @Mock
    private KlimaatSensorRepository klimaatSensorRepository;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private KlimaatSensorValueTrendService klimaatSensorValueTrendService;
    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<RealtimeKlimaat> realtimeKlimaatCaptor;

    @Test
    public void whenGetKlimaatSensorByCodeThenDelegatedToRepository() {
        String sensorCode = "MasterBedroom";
        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode(sensorCode);

        when(klimaatSensorRepository.findFirstByCode(sensorCode)).thenReturn(Optional.of(klimaatSensor));

        assertThat(klimaatService.getKlimaatSensorByCode(sensorCode)).contains(klimaatSensor);
    }

    @Test
    public void whenGetAllKlimaatSensorshenDelegatedToRepository() {
        List<KlimaatSensor> klimaatSensors = asList(mock(KlimaatSensor.class), mock(KlimaatSensor.class));
        when(klimaatSensorRepository.findAll()).thenReturn(klimaatSensors);

        assertThat(klimaatService.getAllKlimaatSensors()).isSameAs(klimaatSensors);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenAddThenRealtimeKlimaatSendToRealtimeTopic() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode("Attic");

        Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(currentDateTime);
        klimaat.setKlimaatSensor(klimaatSensor);
        klimaat.setLuchtvochtigheid(new BigDecimal("56.13"));
        klimaat.setTemperatuur(new BigDecimal("21.51"));

        when(klimaatSensorValueTrendService.determineValueTrend(anyListOf(Klimaat.class), any(Function.class))).then(invocation -> {
            Function<Klimaat, BigDecimal> sensorValueGetter = (Function<Klimaat, BigDecimal>) invocation.getArguments()[1];
            BigDecimal resultOfFunction = sensorValueGetter.apply(klimaat);
            if (resultOfFunction.equals(klimaat.getLuchtvochtigheid())) {
                return Trend.UP;
            } else if (resultOfFunction.equals(klimaat.getTemperatuur())) {
                return Trend.DOWN;
            }
            return null;
        });

        klimaatService.add(klimaat);

        verify(simpMessagingTemplate).convertAndSend(eq(KlimaatService.REALTIME_KLIMAAT_TOPIC), realtimeKlimaatCaptor.capture());

        RealtimeKlimaat realtimeKlimaat = realtimeKlimaatCaptor.getValue();
        assertThat(realtimeKlimaat.getDatumtijd()).isEqualTo(klimaat.getDatumtijd());
        assertThat(realtimeKlimaat.getLuchtvochtigheid()).isEqualTo(klimaat.getLuchtvochtigheid());
        assertThat(realtimeKlimaat.getTemperatuur()).isEqualTo(klimaat.getTemperatuur());
        assertThat(realtimeKlimaat.getLuchtvochtigheidTrend()).isEqualTo(Trend.UP);
        assertThat(realtimeKlimaat.getTemperatuurTrend()).isEqualTo(Trend.DOWN);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenAddThenAddedToRecentlyReceivedKlimaatsPerSensorCode() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = (Map<String, List<Klimaat>>) getField(klimaatService, "recentlyReceivedKlimaatsPerKlimaatSensorCode");

        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode("Basement");

        Klimaat klimaat = aKlimaat().withKlimaatSensor(klimaatSensor)
                                    .withDatumtijd(currentDateTime)
                                    .build();

        klimaatService.add(klimaat);

        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode).containsKeys(klimaatSensor.getCode());
        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensor.getCode())).containsExactly(klimaat);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenAddThenOldItemsRemovedFromRecentlyReceivedKlimaatsPerSensorCode() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode("Basement");

        Klimaat oldKlimaat = aKlimaat().withKlimaatSensor(klimaatSensor)
                                       .withDatumtijd(currentDateTime.minusMinutes(18).minusSeconds(1))
                                       .build();

        Klimaat recentKlimaat = aKlimaat().withKlimaatSensor(klimaatSensor)
                                          .withDatumtijd(currentDateTime.minusMinutes(18))
                                          .build();

        List<Klimaat> recentlyReceivedKlimaats = new ArrayList<>();
        recentlyReceivedKlimaats.add(oldKlimaat);
        recentlyReceivedKlimaats.add(recentKlimaat);

        Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = (Map<String, List<Klimaat>>) getField(klimaatService, "recentlyReceivedKlimaatsPerKlimaatSensorCode");
        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(klimaatSensor.getCode(), recentlyReceivedKlimaats);

        Klimaat klimaatToAdd = aKlimaat().withKlimaatSensor(klimaatSensor)
                                         .withDatumtijd(currentDateTime)
                                         .build();

        klimaatService.add(klimaatToAdd);

        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode).containsKeys(klimaatSensor.getCode());
        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensor.getCode())).containsOnly(klimaatToAdd, recentKlimaat);
    }
}