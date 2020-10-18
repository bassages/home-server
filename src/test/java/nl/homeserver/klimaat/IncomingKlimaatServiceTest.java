package nl.homeserver.klimaat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static nl.homeserver.klimaat.KlimaatSensorBuilder.aKlimaatSensor;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class IncomingKlimaatServiceTest {

    private static final String SOME_SENSOR_CODE = "someSensorCode";

    @InjectMocks
    private IncomingKlimaatService incomingKlimaatService;

    @Mock
    private KlimaatService klimaatService;
    @Mock
    private KlimaatSensorService klimaatSensorService;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private KlimaatSensorValueTrendService klimaatSensorValueTrendService;
    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<RealtimeKlimaat> realtimeKlimaatCaptor;
    @Captor
    private ArgumentCaptor<Klimaat> klimaatCaptor;

    @Before
    public void setup() {
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", klimaatService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenAddThenRealtimeKlimaatSendToRealtimeTopic() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        final Klimaat klimaat = aKlimaat()
                .withDatumtijd(currentDateTime)
                .withKlimaatSensor(klimaatSensor)
                .withLuchtvochtigheid(new BigDecimal("56.13"))
                .withTemperatuur(new BigDecimal("21.51"))
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
        assertThat(realtimeKlimaat.getDatumtijd()).isEqualTo(klimaat.getDatumtijd());
        assertThat(realtimeKlimaat.getLuchtvochtigheid()).isEqualTo(klimaat.getLuchtvochtigheid());
        assertThat(realtimeKlimaat.getTemperatuur()).isEqualTo(klimaat.getTemperatuur());
        assertThat(realtimeKlimaat.getLuchtvochtigheidTrend()).isEqualTo(Trend.UP);
        assertThat(realtimeKlimaat.getTemperatuurTrend()).isEqualTo(Trend.DOWN);
        assertThat(realtimeKlimaat.getSensorCode()).isEqualTo(klimaatSensor.getCode());
    }

    @Test
    public void whenAddThenAddedToRecentlyReceivedKlimaatsPerSensorCode() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        final Klimaat klimaat = aKlimaat().withKlimaatSensor(klimaatSensor)
                                          .withDatumtijd(currentDateTime)
                                          .build();

        incomingKlimaatService.add(klimaat);

        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode).containsKeys(klimaatSensor.getCode());
        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensor.getCode())).containsExactly(klimaat);
    }

    @Test
    public void givenKlimaatWithoutDatumtijdWhenAddThenDatumtijdIsSetToCurrent() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        final Klimaat klimaat = aKlimaat().withKlimaatSensor(klimaatSensor)
                                          .withDatumtijd(null)
                                          .build();

        incomingKlimaatService.add(klimaat);

        assertThat(klimaat.getDatumtijd()).isEqualTo(currentDateTime);
    }

    @Test
    public void whenAddThenOldItemsRemovedFromRecentlyReceivedKlimaatsPerSensorCode() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        final Klimaat oldKlimaat = aKlimaat().withKlimaatSensor(klimaatSensor)
                                             .withDatumtijd(currentDateTime.minusMinutes(18).minusSeconds(1))
                                             .build();

        final Klimaat recentKlimaat = aKlimaat().withKlimaatSensor(klimaatSensor)
                                                .withDatumtijd(currentDateTime.minusMinutes(18))
                                                .build();

        final List<Klimaat> recentlyReceivedKlimaats = new ArrayList<>();
        recentlyReceivedKlimaats.add(oldKlimaat);
        recentlyReceivedKlimaats.add(recentKlimaat);

        final Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();
        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(klimaatSensor.getCode(), recentlyReceivedKlimaats);

        final Klimaat klimaatToAdd = aKlimaat().withKlimaatSensor(klimaatSensor)
                                               .withDatumtijd(currentDateTime)
                                               .build();

        incomingKlimaatService.add(klimaatToAdd);

        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode).containsKeys(klimaatSensor.getCode());
        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(SOME_SENSOR_CODE)).containsOnly(klimaatToAdd, recentKlimaat);
    }

    @Test
    public void whenSaveThenOneKlimaatPerSensorSavedWithAverageSensorValues() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atTime(10, 15, 2);
        timeTravelTo(clock, currentDateTime);

        final Map<String, List<Klimaat>> recentlyAddedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        when(klimaatSensorService.getOrCreateIfNonExists(SOME_SENSOR_CODE)).thenReturn(klimaatSensor);

        final Klimaat recentValidKlimaat1 = aKlimaat().withKlimaatSensor(klimaatSensor)
                                                      .withDatumtijd(currentDateTime.minusMinutes(10))
                                                      .withLuchtvochtigheid(new BigDecimal("25.00"))
                                                      .withTemperatuur(new BigDecimal("20.00"))
                                                      .build();
        final Klimaat recentValidKlimaat2 = aKlimaat().withKlimaatSensor(klimaatSensor)
                                                      .withDatumtijd(currentDateTime.minusMinutes(5))
                                                      .withLuchtvochtigheid(new BigDecimal("75.00"))
                                                      .withTemperatuur(new BigDecimal("10.00"))
                                                      .build();
        final Klimaat recentInvalidKlimaat1 = aKlimaat().withKlimaatSensor(klimaatSensor)
                                                        .withDatumtijd(currentDateTime.minusMinutes(4))
                                                        .withLuchtvochtigheid(ZERO) // ZERO = invalid
                                                        .withTemperatuur(ZERO) // ZERO = invalid
                                                        .build();
        final  Klimaat recentInvalidKlimaat2 = aKlimaat().withKlimaatSensor(klimaatSensor)
                                                         .withDatumtijd(currentDateTime.minusMinutes(3))
                                                         .withLuchtvochtigheid(null) // null = invalid
                                                         .withTemperatuur(null) // null = invalid
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
    public void givenNoRecentlyReceivedKlimaatWhenGetMostRecentThenMostRecentIsNull() {
        final RealtimeKlimaat mostRecent = incomingKlimaatService.getMostRecent(SOME_SENSOR_CODE);

        assertThat(mostRecent).isNull();
    }

    @Test
    public void givenMultipleRecentlyAddedKlimaatWhenGetMostRecentThenMostRecentIsReturned() {
        final LocalDate date = LocalDate.of(2016, SEPTEMBER, 1);
        timeTravelTo(clock, date.atStartOfDay());

        final Klimaat klimaat1 = aKlimaat().withDatumtijd(date.atTime(12, 14, 41)).build();
        final Klimaat klimaat2 = aKlimaat().withKlimaatSensor(aKlimaatSensor().withCode(SOME_SENSOR_CODE).build())
                                           .withDatumtijd(date.atTime(23, 26, 8))
                                           .withTemperatuur(new BigDecimal("23.7"))
                                           .withLuchtvochtigheid(new BigDecimal("45.7"))
                                           .build();
        final Klimaat klimaat3 = aKlimaat().withDatumtijd(date.atTime(2, 0, 45)).build();

        getRecentlyReceivedKlimaatPerSensorCode().put(SOME_SENSOR_CODE, List.of(klimaat1, klimaat2, klimaat3));

        final RealtimeKlimaat mostRecent = incomingKlimaatService.getMostRecent(SOME_SENSOR_CODE);
        assertThat(mostRecent.getSensorCode()).isEqualTo(SOME_SENSOR_CODE);
        assertThat(mostRecent.getDatumtijd()).isEqualTo(klimaat2.getDatumtijd());
        assertThat(mostRecent.getTemperatuur()).isEqualTo(klimaat2.getTemperatuur());
        assertThat(mostRecent.getLuchtvochtigheid()).isEqualTo(klimaat2.getLuchtvochtigheid());
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Klimaat>> getRecentlyReceivedKlimaatPerSensorCode() {
        return (Map<String, List<Klimaat>>) getField(incomingKlimaatService, "recentlyAddedKlimaatsPerKlimaatSensorCode");
    }
}
