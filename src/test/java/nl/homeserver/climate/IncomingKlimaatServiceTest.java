package nl.homeserver.climate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.JANUARY;
import static java.time.Month.SEPTEMBER;
import static nl.homeserver.climate.Klimaat.aKlimaat;
import static nl.homeserver.climate.KlimaatSensor.aKlimaatSensor;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;

@ExtendWith(MockitoExtension.class)
class IncomingKlimaatServiceTest {

    private static final String SOME_SENSOR_CODE = "someSensorCode";

    @InjectMocks
    IncomingKlimaatService incomingKlimaatService;

    @Mock
    KlimaatService klimaatService;
    @Mock
    KlimaatSensorService klimaatSensorService;
    @Mock
    SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    KlimaatSensorValueTrendService klimaatSensorValueTrendService;
    @Mock
    Clock clock;

    @Captor
    ArgumentCaptor<RealtimeKlimaat> realtimeKlimaatCaptor;
    @Captor
    ArgumentCaptor<Klimaat> klimaatCaptor;

    @SuppressWarnings("unchecked")
    @Test
    void whenAddThenRealtimeKlimaatSendToRealtimeTopic() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().code(SOME_SENSOR_CODE).build();

        final Klimaat klimaat = aKlimaat().datumtijd(currentDateTime)
                                          .klimaatSensor(klimaatSensor)
                                          .luchtvochtigheid(new BigDecimal("56.13"))
                                          .temperatuur(new BigDecimal("21.51"))
                                          .build();

        when(klimaatSensorValueTrendService.determineValueTrend(anyList(), any(Function.class))).then(invocation -> {
            final Function<Klimaat, BigDecimal> sensorValueGetter = (Function<Klimaat, BigDecimal>) invocation.getArguments()[1];
            final BigDecimal resultOfFunction = sensorValueGetter.apply(klimaat);
            if (resultOfFunction.equals(klimaat.getLuchtvochtigheid())) {
                return Trend.UP;
            } else if (resultOfFunction.equals(klimaat.getTemperatuur())) {
                return Trend.DOWN;
            }
            return null;
        });

        incomingKlimaatService.add(klimaat);

        verify(simpMessagingTemplate).convertAndSend(eq(IncomingKlimaatService.REALTIME_KLIMAAT_TOPIC), realtimeKlimaatCaptor.capture());

        final RealtimeKlimaat realtimeKlimaat = realtimeKlimaatCaptor.getValue();
        assertThat(realtimeKlimaat.datumtijd()).isEqualTo(klimaat.getDatumtijd());
        assertThat(realtimeKlimaat.luchtvochtigheid()).isEqualTo(klimaat.getLuchtvochtigheid());
        assertThat(realtimeKlimaat.temperatuur()).isEqualTo(klimaat.getTemperatuur());
        assertThat(realtimeKlimaat.luchtvochtigheidTrend()).isEqualTo(Trend.UP);
        assertThat(realtimeKlimaat.temperatuurTrend()).isEqualTo(Trend.DOWN);
        assertThat(realtimeKlimaat.sensorCode()).isEqualTo(klimaatSensor.getCode());
    }

    @Test
    void whenAddThenAddedToRecentlyReceivedKlimaatsPerSensorCode() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();

        final KlimaatSensor klimaatSensor = aKlimaatSensor().code(SOME_SENSOR_CODE).build();

        final Klimaat klimaat = aKlimaat().klimaatSensor(klimaatSensor)
                                          .datumtijd(currentDateTime)
                                          .build();

        incomingKlimaatService.add(klimaat);

        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode).containsKeys(klimaatSensor.getCode());
        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensor.getCode())).containsExactly(klimaat);
    }

    @Test
    void givenKlimaatWithoutDatumtijdWhenAddThenDatumtijdIsSetToCurrent() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().code(SOME_SENSOR_CODE).build();

        final Klimaat klimaat = aKlimaat().klimaatSensor(klimaatSensor)
                                          .datumtijd(null)
                                          .build();

        incomingKlimaatService.add(klimaat);

        assertThat(klimaat.getDatumtijd()).isEqualTo(currentDateTime);
    }

    @Test
    void whenAddThenOldItemsRemovedFromRecentlyReceivedKlimaatsPerSensorCode() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().code(SOME_SENSOR_CODE).build();

        final Klimaat oldKlimaat = aKlimaat().klimaatSensor(klimaatSensor)
                                             .datumtijd(currentDateTime.minusMinutes(18).minusSeconds(1))
                                             .build();

        final Klimaat recentKlimaat = aKlimaat().klimaatSensor(klimaatSensor)
                                                .datumtijd(currentDateTime.minusMinutes(18))
                                                .build();

        final List<Klimaat> recentlyReceivedKlimaats = new ArrayList<>();
        recentlyReceivedKlimaats.add(oldKlimaat);
        recentlyReceivedKlimaats.add(recentKlimaat);

        final Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();
        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(klimaatSensor.getCode(), recentlyReceivedKlimaats);

        final Klimaat klimaatToAdd = aKlimaat().klimaatSensor(klimaatSensor)
                                               .datumtijd(currentDateTime)
                                               .build();

        incomingKlimaatService.add(klimaatToAdd);

        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode).containsKeys(klimaatSensor.getCode());
        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(SOME_SENSOR_CODE)).containsOnly(klimaatToAdd, recentKlimaat);
    }

    @Test
    void whenSaveThenOneKlimaatPerSensorSavedWithAverageSensorValues() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atTime(10, 15, 2);
        timeTravelTo(clock, currentDateTime);

        final Map<String, List<Klimaat>> recentlyAddedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();

        final KlimaatSensor klimaatSensor = aKlimaatSensor().code(SOME_SENSOR_CODE).build();

        when(klimaatSensorService.getOrCreateIfNonExists(SOME_SENSOR_CODE)).thenReturn(klimaatSensor);

        final Klimaat recentValidKlimaat1 = aKlimaat().klimaatSensor(klimaatSensor)
                                                      .datumtijd(currentDateTime.minusMinutes(10))
                                                      .luchtvochtigheid(new BigDecimal("25.00"))
                                                      .temperatuur(new BigDecimal("20.00"))
                                                      .build();
        final Klimaat recentValidKlimaat2 = aKlimaat().klimaatSensor(klimaatSensor)
                                                      .datumtijd(currentDateTime.minusMinutes(5))
                                                      .luchtvochtigheid(new BigDecimal("75.00"))
                                                      .temperatuur(new BigDecimal("10.00"))
                                                      .build();
        final Klimaat recentInvalidKlimaat1 = aKlimaat().klimaatSensor(klimaatSensor)
                                                        .datumtijd(currentDateTime.minusMinutes(4))
                                                        .luchtvochtigheid(ZERO) // ZERO = invalid
                                                        .temperatuur(ZERO) // ZERO = invalid
                                                        .build();
        final  Klimaat recentInvalidKlimaat2 = aKlimaat().klimaatSensor(klimaatSensor)
                                                         .datumtijd(currentDateTime.minusMinutes(3))
                                                         .luchtvochtigheid(null) // null = invalid
                                                         .temperatuur(null) // null = invalid
                                                         .build();

        final List<Klimaat> klimaats = List.of(recentValidKlimaat1, recentValidKlimaat2,
                                               recentInvalidKlimaat1, recentInvalidKlimaat2);
        recentlyAddedKlimaatsPerKlimaatSensorCode.put(klimaatSensor.getCode(), klimaats);

        // when
        incomingKlimaatService.save();

        // then
        verify(klimaatService).save(klimaatCaptor.capture());

        final Klimaat savedKlimaat = klimaatCaptor.getValue();
        assertThat(savedKlimaat.getDatumtijd()).isEqualTo(LocalDate.of(2016, JANUARY, 1).atTime(10, 15, 0));
        assertThat(savedKlimaat.getLuchtvochtigheid()).isEqualTo(new BigDecimal("50.0"));
        assertThat(savedKlimaat.getTemperatuur()).isEqualTo(new BigDecimal("15.00"));
        assertThat(savedKlimaat.getKlimaatSensor()).isEqualTo(klimaatSensor);
    }

    @Test
    void givenNoRecentlyReceivedKlimaatWhenGetMostRecentThenMostRecentIsNull() {
        final RealtimeKlimaat mostRecent = incomingKlimaatService.getMostRecent(SOME_SENSOR_CODE);

        assertThat(mostRecent).isNull();
    }

    @Test
    void givenMultipleRecentlyAddedKlimaatWhenGetMostRecentThenMostRecentIsReturned() {
        final LocalDate date = LocalDate.of(2016, SEPTEMBER, 1);
        timeTravelTo(clock, date.atStartOfDay());

        final Klimaat klimaat1 = aKlimaat().datumtijd(date.atTime(12, 14, 41)).build();
        final Klimaat klimaat2 = aKlimaat().klimaatSensor(aKlimaatSensor().code(SOME_SENSOR_CODE).build())
                                           .datumtijd(date.atTime(23, 26, 8))
                                           .temperatuur(new BigDecimal("23.7"))
                                           .luchtvochtigheid(new BigDecimal("45.7"))
                                           .build();
        final Klimaat klimaat3 = aKlimaat().datumtijd(date.atTime(2, 0, 45)).build();

        getRecentlyReceivedKlimaatPerSensorCode().put(SOME_SENSOR_CODE, List.of(klimaat1, klimaat2, klimaat3));

        final RealtimeKlimaat mostRecent = incomingKlimaatService.getMostRecent(SOME_SENSOR_CODE);
        assertThat(mostRecent.sensorCode()).isEqualTo(SOME_SENSOR_CODE);
        assertThat(mostRecent.datumtijd()).isEqualTo(klimaat2.getDatumtijd());
        assertThat(mostRecent.temperatuur()).isEqualTo(klimaat2.getTemperatuur());
        assertThat(mostRecent.luchtvochtigheid()).isEqualTo(klimaat2.getLuchtvochtigheid());
    }

    @SuppressWarnings("unchecked")
    Map<String, List<Klimaat>> getRecentlyReceivedKlimaatPerSensorCode() {
        return (Map<String, List<Klimaat>>) getField(incomingKlimaatService, "recentlyAddedKlimaatsPerKlimaatSensorCode");
    }
}
