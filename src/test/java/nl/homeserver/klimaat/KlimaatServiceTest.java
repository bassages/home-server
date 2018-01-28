package nl.homeserver.klimaat;

import static java.time.Month.JANUARY;
import static java.time.Month.SEPTEMBER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static nl.homeserver.klimaat.SensorType.LUCHTVOCHTIGHEID;
import static nl.homeserver.klimaat.SensorType.TEMPERATUUR;
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
import java.sql.Date;
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
    private KlimaatRepos klimaatRepository;
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
    @Captor
    private ArgumentCaptor<Klimaat> klimaatCaptor;
    @Captor
    private ArgumentCaptor<KlimaatSensor> klimaatSensorCaptor;

    @Test
    public void whenGetKlimaatSensorByCodeThenDelegatedToRepository() {
        String sensorCode = "MasterBedroom";
        KlimaatSensor klimaatSensor = createKlimaatSensor(sensorCode);

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

        KlimaatSensor klimaatSensor = createKlimaatSensor("Attic");

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

        KlimaatSensor klimaatSensor = createKlimaatSensor("Basement");

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

        KlimaatSensor klimaatSensor = createKlimaatSensor("Basement");

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

    @SuppressWarnings("unchecked")
    @Test
    public void givenKlimaatOfUnUnknowSensorWhenSaveThenKlimaatSensorCreated() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        String klimaatSensorCode = "This one is unknown";
        KlimaatSensor klimaatSensor = createKlimaatSensor(klimaatSensorCode);

        Klimaat klimaatToSave = aKlimaat().withKlimaatSensor(klimaatSensor)
                                          .withDatumtijd(currentDateTime)
                                          .build();

        Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = (Map<String, List<Klimaat>>) getField(klimaatService, "recentlyReceivedKlimaatsPerKlimaatSensorCode");
        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(klimaatSensorCode, singletonList(klimaatToSave));

        when(klimaatSensorRepository.findFirstByCode(klimaatSensorCode)).thenReturn(Optional.empty());

        klimaatService.save();

        verify(klimaatSensorRepository).save(klimaatSensorCaptor.capture());
        KlimaatSensor createdKlimaatSensor = klimaatSensorCaptor.getValue();
        assertThat(createdKlimaatSensor.getCode()).isEqualTo(klimaatSensorCode);
        assertThat(createdKlimaatSensor.getOmschrijving()).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenSaveThenOneKlimaatPerSensorSavedWithAverageSensorValues() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atTime(10, 15, 2);
        timeTravelTo(clock, currentDateTime);

        Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = (Map<String, List<Klimaat>>) getField(klimaatService, "recentlyReceivedKlimaatsPerKlimaatSensorCode");

        KlimaatSensor klimaatSensorBasement = createKlimaatSensor("Basement");

        when(klimaatSensorRepository.findFirstByCode(klimaatSensorBasement.getCode())).thenReturn(Optional.of(klimaatSensorBasement));

        Klimaat recentKlimaat1ForBasement = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                      .withDatumtijd(currentDateTime.minusMinutes(10))
                                                      .withLuchtvochtigheid(new BigDecimal("25.00"))
                                                      .withTemperatuur(new BigDecimal("20.00"))
                                                      .build();
        Klimaat recentKlimaat2ForBasement = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                      .withDatumtijd(currentDateTime.minusMinutes(5))
                                                      .withLuchtvochtigheid(new BigDecimal("75.00"))
                                                      .withTemperatuur(new BigDecimal("10.00"))
                                                      .build();

        List<Klimaat> recentlyReceivedKlimaatsForBasement = new ArrayList<>();
        recentlyReceivedKlimaatsForBasement.add(recentKlimaat1ForBasement);
        recentlyReceivedKlimaatsForBasement.add(recentKlimaat2ForBasement);
        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(klimaatSensorBasement.getCode(), recentlyReceivedKlimaatsForBasement);

        klimaatService.save();

        verify(klimaatRepository).save(klimaatCaptor.capture());

        Klimaat savedKlimaat = klimaatCaptor.getValue();
        assertThat(savedKlimaat.getDatumtijd()).isEqualTo(LocalDate.of(2016, JANUARY, 1).atTime(10, 15, 0));
        assertThat(savedKlimaat.getLuchtvochtigheid()).isEqualTo(new BigDecimal("50.0"));
        assertThat(savedKlimaat.getTemperatuur()).isEqualTo(new BigDecimal("15.00"));
    }

    @Test
    public void whenGetHighestTemperatureThenDelegatedToRepository() {
        String sensorCode = "DogHouse";
        LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        LocalDate to = from.plusDays(1);
        int limit = 100;

        Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakHighTemperatureDates(sensorCode, from, to, limit)).thenReturn(singletonList(day));
        when(klimaatRepository.earliestHighestTemperatureOnDay(sensorCode, from)).thenReturn(klimaat);

        List<Klimaat> highest = klimaatService.getHighest(sensorCode, TEMPERATUUR, aPeriodWithToDate(from, to), limit);

        assertThat(highest).containsExactly(klimaat);
    }

    @Test
    public void whenGetLowestTemperatureThenDelegatedToRepository() {
        String sensorCode = "DogHouse";
        LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        LocalDate to = from.plusDays(1);
        int limit = 100;

        Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakLowTemperatureDates(sensorCode, from, to, limit)).thenReturn(singletonList(day));
        when(klimaatRepository.earliestLowestTemperatureOnDay(sensorCode, from)).thenReturn(klimaat);

        List<Klimaat> lowest = klimaatService.getLowest(sensorCode, TEMPERATUUR, aPeriodWithToDate(from, to), limit);

        assertThat(lowest).containsExactly(klimaat);
    }

    @Test
    public void whenGetHighestHumidityThenDelegatedToRepository() {
        String sensorCode = "DogHouse";
        LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        LocalDate to = from.plusDays(1);
        int limit = 100;

        Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakHighHumidityDates(sensorCode, from, to, limit)).thenReturn(singletonList(day));
        when(klimaatRepository.earliestHighestHumidityOnDay(sensorCode, from)).thenReturn(klimaat);

        List<Klimaat> highest = klimaatService.getHighest(sensorCode, LUCHTVOCHTIGHEID, aPeriodWithToDate(from, to), limit);

        assertThat(highest).containsExactly(klimaat);
    }

    @Test
    public void whenGetLowestHumidityThenDelegatedToRepository() {
        String sensorCode = "DogHouse";
        LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        LocalDate to = from.plusDays(1);
        int limit = 100;

        Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakLowHumidityDates(sensorCode, from, to, limit)).thenReturn(singletonList(day));
        when(klimaatRepository.earliestLowestHumidityOnDay(sensorCode, from)).thenReturn(klimaat);

        List<Klimaat> lowest = klimaatService.getLowest(sensorCode, LUCHTVOCHTIGHEID, aPeriodWithToDate(from, to), limit);

        assertThat(lowest).containsExactly(klimaat);
    }

    private KlimaatSensor createKlimaatSensor(String sensorCode) {
        KlimaatSensor klimaatSensorBasement = new KlimaatSensor();
        klimaatSensorBasement.setCode(sensorCode);
        return klimaatSensorBasement;
    }
}