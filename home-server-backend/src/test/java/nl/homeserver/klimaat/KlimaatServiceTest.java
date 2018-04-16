package nl.homeserver.klimaat;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.SEPTEMBER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static nl.homeserver.klimaat.SensorType.LUCHTVOCHTIGHEID;
import static nl.homeserver.klimaat.SensorType.TEMPERATUUR;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import nl.homeserver.DatePeriod;
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

    @Before
    public void setup() {
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", klimaatService);
    }

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

        when(klimaatSensorValueTrendService.determineValueTrend(anyList(), any(Function.class))).then(invocation -> {
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
        assertThat(realtimeKlimaat.getSensorCode()).isEqualTo(klimaatSensor.getCode());
    }

    @Test
    public void whenAddThenAddedToRecentlyReceivedKlimaatsPerSensorCode() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();

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

        Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();
        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(klimaatSensor.getCode(), recentlyReceivedKlimaats);

        Klimaat klimaatToAdd = aKlimaat().withKlimaatSensor(klimaatSensor)
                                         .withDatumtijd(currentDateTime)
                                         .build();

        klimaatService.add(klimaatToAdd);

        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode).containsKeys(klimaatSensor.getCode());
        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensor.getCode())).containsOnly(klimaatToAdd, recentKlimaat);
    }

    @Test
    public void givenKlimaatOfUnUnknowSensorWhenSaveThenKlimaatSensorCreated() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        String klimaatSensorCode = "This one is unknown";
        KlimaatSensor klimaatSensor = createKlimaatSensor(klimaatSensorCode);

        Klimaat klimaatToSave = aKlimaat().withKlimaatSensor(klimaatSensor)
                                          .withDatumtijd(currentDateTime)
                                          .build();

        Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();
        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(klimaatSensorCode, singletonList(klimaatToSave));

        when(klimaatSensorRepository.findFirstByCode(klimaatSensorCode)).thenReturn(Optional.empty());

        klimaatService.save();

        verify(klimaatSensorRepository).save(klimaatSensorCaptor.capture());
        KlimaatSensor createdKlimaatSensor = klimaatSensorCaptor.getValue();
        assertThat(createdKlimaatSensor.getCode()).isEqualTo(klimaatSensorCode);
        assertThat(createdKlimaatSensor.getOmschrijving()).isNull();
    }

    @Test
    public void whenSaveThenOneKlimaatPerSensorSavedWithAverageSensorValues() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atTime(10, 15, 2);
        timeTravelTo(clock, currentDateTime);

        Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();

        KlimaatSensor klimaatSensorBasement = createKlimaatSensor("Basement");

        when(klimaatSensorRepository.findFirstByCode(klimaatSensorBasement.getCode())).thenReturn(Optional.of(klimaatSensorBasement));

        Klimaat recentValidKlimaat1 = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                .withDatumtijd(currentDateTime.minusMinutes(10))
                                                .withLuchtvochtigheid(new BigDecimal("25.00"))
                                                .withTemperatuur(new BigDecimal("20.00"))
                                                .build();
        Klimaat recentValidKlimaat2 = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                .withDatumtijd(currentDateTime.minusMinutes(5))
                                                .withLuchtvochtigheid(new BigDecimal("75.00"))
                                                .withTemperatuur(new BigDecimal("10.00"))
                                                .build();
        Klimaat recentInvalidKlimaat1 = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                  .withDatumtijd(currentDateTime.minusMinutes(4))
                                                  .withLuchtvochtigheid(ZERO)
                                                  .withTemperatuur(ZERO)
                                                  .build();
        Klimaat recentInvalidKlimaat2 = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                  .withDatumtijd(currentDateTime.minusMinutes(3))
                                                  .withLuchtvochtigheid(null)
                                                  .withTemperatuur(null)
                                                  .build();

        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(klimaatSensorBasement.getCode(),
                                                         asList(recentValidKlimaat1, recentValidKlimaat2, recentInvalidKlimaat1, recentInvalidKlimaat2));

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

    @Test
    public void givenUnexpectedSensorTypewhenGetHighestThenException() {
        LocalDate from = LocalDate.of(2016, SEPTEMBER, 10);
        LocalDate to = from.plusDays(1);
        int limit = 100;

        SensorType unexpectedSensorType = null;
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> klimaatService.getLowest("someSensorCode", unexpectedSensorType, aPeriodWithToDate(from, to), limit))
                .withMessage("Unexpected SensorType [null]");
    }

    @Test
    public void givenUnexpectedSensorTypewhenGetLowestThenException() {
        LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        LocalDate to = from.plusDays(1);
        int limit = 100;

        SensorType unexpectedSensorType = null;
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> klimaatService.getHighest("someSensorCode", unexpectedSensorType, aPeriodWithToDate(from, to), limit))
                .withMessage("Unexpected SensorType [null]");
    }

    @Test
    public void givenNoRecentlyReceivedKlimaatWhenGetMostRecentThenMostRecentIsNull() {
        String sensorCode = "Barn";

        RealtimeKlimaat mostRecent = klimaatService.getMostRecent(sensorCode);

        assertThat(mostRecent).isNull();
    }

    @Test
    public void givenMultipleRecentlyReceivedKLimaatWhenGetMostRecentThenMostRecentIsReturned() {
        LocalDate date = LocalDate.of(2016, SEPTEMBER, 1);
        timeTravelTo(clock, date.atStartOfDay());

        String sensorCode = "Kitchen";

        Klimaat klimaat1 = aKlimaat().withDatumtijd(date.atTime(12, 14, 41)).build();
        Klimaat klimaat2 = aKlimaat().withKlimaatSensor(createKlimaatSensor(sensorCode))
                                     .withDatumtijd(date.atTime(23, 26, 8))
                                     .withTemperatuur(new BigDecimal("23.7"))
                                     .withLuchtvochtigheid(new BigDecimal("45.7"))
                                     .build();
        Klimaat klimaat3 = aKlimaat().withDatumtijd(date.atTime(2, 0, 45)).build();

        getRecentlyReceivedKlimaatPerSensorCode().put(sensorCode, asList(klimaat1, klimaat2, klimaat3));

        RealtimeKlimaat mostRecent = klimaatService.getMostRecent(sensorCode);
        assertThat(mostRecent.getDatumtijd()).isEqualTo(klimaat2.getDatumtijd());
        assertThat(mostRecent.getTemperatuur()).isEqualTo(klimaat2.getTemperatuur());
        assertThat(mostRecent.getLuchtvochtigheid()).isEqualTo(klimaat2.getLuchtvochtigheid());
    }

    @Test
    public void whenGetAverageHumidityPerMonthInYearsThenAveragesReturned() {
        String sensorCode = "MasterBedroom";
        int[] years = {2017, 2018};

        BigDecimal averageHumidityInJune2017 = new BigDecimal("22.18");
        when(klimaatRepository.getAverageLuchtvochtigheid(sensorCode, LocalDate.of(2017, JUNE, 1).atStartOfDay(), LocalDate.of(2017, JULY, 1).atStartOfDay()))
                              .thenReturn(averageHumidityInJune2017);

        List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(sensorCode, SensorType.LUCHTVOCHTIGHEID, years);

        assertThat(averagePerMonthInYears).hasSize(2);

        List<GemiddeldeKlimaatPerMaand> averagesIn2017 = averagePerMonthInYears.get(0);
        assertThat(averagesIn2017).hasSize(12);
        for (Month month : Month.values()) {
            assertThat(averagesIn2017.get(month.ordinal()).getMaand()).isEqualTo(LocalDate.of(2017, month, 1));
        }
        assertThat(averagesIn2017.get(JUNE.ordinal()).getGemiddelde()).isEqualTo(averageHumidityInJune2017);

        List<GemiddeldeKlimaatPerMaand> averagesIn2018 = averagePerMonthInYears.get(1);
        assertThat(averagesIn2018).hasSize(12);
        for (Month month : Month.values()) {
            assertThat(averagesIn2018.get(month.ordinal()).getMaand()).isEqualTo(LocalDate.of(2018, month, 1));
        }
    }

    @Test
    public void whenGetAverageTemperaturePerMonthInYearsThenAveragesReturned() {
        String sensorCode = "MasterBedroom";
        int[] years = {2016};

        BigDecimal averageTemperatureInJune2016 = new BigDecimal("22.18");
        when(klimaatRepository.getAverageTemperatuur(sensorCode, LocalDate.of(2016, JUNE, 1).atStartOfDay(), LocalDate.of(2016, JULY, 1).atStartOfDay()))
                              .thenReturn(averageTemperatureInJune2016);

        List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(sensorCode, SensorType.TEMPERATUUR, years);

        assertThat(averagePerMonthInYears).hasSize(1);

        List<GemiddeldeKlimaatPerMaand> averagesIn2016 = averagePerMonthInYears.get(0);
        assertThat(averagesIn2016).hasSize(12);
        for (Month month : Month.values()) {
            assertThat(averagesIn2016.get(month.ordinal()).getMaand()).isEqualTo(LocalDate.of(2016, month, 1));
        }

        assertThat(averagesIn2016.get(JUNE.ordinal()).getGemiddelde()).isEqualTo(averageTemperatureInJune2016);
    }

    @Test
    public void givenInvalidSensortypewhenGetAverageTemperaturePerMonthInYearsThenExceptionThrown() {
        int[] someYears = {2015};

        SensorType invalidSensorType = null;

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> klimaatService.getAveragePerMonthInYears("SomeSomeSensor", invalidSensorType, someYears))
            .withMessage("Unexpected SensorType [null]");
    }

    @Test
    public void whenGetInPeriodBeforeCurrentDateTimeThenRetrievedFromCache() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        KlimaatService cachedProxy = mock(KlimaatService.class);
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", cachedProxy);

        String klimaatSensorCode = "someKlimaatSensor";
        DatePeriod period = aPeriodWithToDate(currentDateTime.minusDays(1).toLocalDate(), currentDateTime.toLocalDate());

        List<Klimaat> cachedKlimaats = asList(mock(Klimaat.class), mock(Klimaat.class));
        when(cachedProxy.getPotentiallyCachedAllInPeriod(eq(klimaatSensorCode), eq(period))).thenReturn(cachedKlimaats);

        assertThat(klimaatService.getInPeriod(klimaatSensorCode, period)).isSameAs(cachedKlimaats);
    }

    @Test
    public void whenGetInPeriodIncludingCurrentDateThenRetrievedFromRepository() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        String klimaatSensorCode = "someKlimaatSensor";
        DatePeriod period = aPeriodWithToDate(currentDateTime.toLocalDate(), currentDateTime.plusDays(1).toLocalDate());

        List<Klimaat> klimaatsFromRepository = asList(mock(Klimaat.class), mock(Klimaat.class));
        when(klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode, period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay().minusNanos(1)))
                              .thenReturn(klimaatsFromRepository);

        assertThat(klimaatService.getInPeriod(klimaatSensorCode, period)).isSameAs(klimaatsFromRepository);
    }

    @Test
    public void whenGetPotentiallyCachedAllInPeriodThenRetrievedFromRepository() {
        LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        String klimaatSensorCode = "someKlimaatSensor";
        DatePeriod period = aPeriodWithToDate(currentDateTime.toLocalDate(), currentDateTime.plusDays(1).toLocalDate());

        List<Klimaat> klimaatsFromRepository = asList(mock(Klimaat.class), mock(Klimaat.class));
        when(klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode, period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay().minusNanos(1)))
                .thenReturn(klimaatsFromRepository);

        assertThat(klimaatService.getPotentiallyCachedAllInPeriod(klimaatSensorCode, period)).isSameAs(klimaatsFromRepository);
    }

    private KlimaatSensor createKlimaatSensor(String sensorCode) {
        KlimaatSensor klimaatSensorBasement = new KlimaatSensor();
        klimaatSensorBasement.setCode(sensorCode);
        return klimaatSensorBasement;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Klimaat>> getRecentlyReceivedKlimaatPerSensorCode() {
        return (Map<String, List<Klimaat>>) getField(klimaatService, "recentlyReceivedKlimaatsPerKlimaatSensorCode");
    }
}