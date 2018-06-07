package nl.homeserver.klimaat;

import nl.homeserver.DatePeriod;
import nl.homeserver.Trend;
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

import static java.math.BigDecimal.ZERO;
import static java.time.Month.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static nl.homeserver.klimaat.KlimaatSensorBuilder.aKlimaatSensor;
import static nl.homeserver.klimaat.SensorType.LUCHTVOCHTIGHEID;
import static nl.homeserver.klimaat.SensorType.TEMPERATUUR;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class KlimaatServiceTest {

    private static final String SOME_SENSOR_CODE = "someSensorCode";

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
        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        when(klimaatSensorRepository.findFirstByCode(SOME_SENSOR_CODE)).thenReturn(Optional.of(klimaatSensor));

        assertThat(klimaatService.getKlimaatSensorByCode(SOME_SENSOR_CODE)).contains(klimaatSensor);
    }

    @Test
    public void whenGetAllKlimaatSensorshenDelegatedToRepository() {
        final List<KlimaatSensor> klimaatSensors = asList(mock(KlimaatSensor.class), mock(KlimaatSensor.class));
        when(klimaatSensorRepository.findAll()).thenReturn(klimaatSensors);

        assertThat(klimaatService.getAllKlimaatSensors()).isSameAs(klimaatSensors);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenAddThenRealtimeKlimaatSendToRealtimeTopic() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        final Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(currentDateTime);
        klimaat.setKlimaatSensor(klimaatSensor);
        klimaat.setLuchtvochtigheid(new BigDecimal("56.13"));
        klimaat.setTemperatuur(new BigDecimal("21.51"));

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

        klimaatService.add(klimaat);

        verify(simpMessagingTemplate).convertAndSend(eq(KlimaatService.REALTIME_KLIMAAT_TOPIC), realtimeKlimaatCaptor.capture());

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

        klimaatService.add(klimaat);

        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode).containsKeys(klimaatSensor.getCode());
        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensor.getCode())).containsExactly(klimaat);
    }

    @Test
    public void givenKlimaatWithoutDatumtijdwhenAddThenDatumtijdIsSetToCurrent() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        final Klimaat klimaat = aKlimaat().withKlimaatSensor(klimaatSensor)
                                          .withDatumtijd(null)
                                          .build();

        klimaatService.add(klimaat);

        assertThat(klimaat.getDatumtijd()).isEqualTo(currentDateTime);
    }

    @SuppressWarnings("unchecked")
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

        klimaatService.add(klimaatToAdd);

        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode).containsKeys(klimaatSensor.getCode());
        assertThat(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensor.getCode())).containsOnly(klimaatToAdd, recentKlimaat);
    }

    @Test
    public void givenKlimaatOfUnUnknowSensorWhenSaveThenKlimaatSensorCreated() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        final Klimaat klimaatToSave = aKlimaat().withKlimaatSensor(klimaatSensor)
                                                .withDatumtijd(currentDateTime)
                                                .build();

        final Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();
        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(SOME_SENSOR_CODE, singletonList(klimaatToSave));

        when(klimaatSensorRepository.findFirstByCode(SOME_SENSOR_CODE)).thenReturn(Optional.empty());

        klimaatService.save();

        verify(klimaatSensorRepository).save(klimaatSensorCaptor.capture());
        final KlimaatSensor createdKlimaatSensor = klimaatSensorCaptor.getValue();
        assertThat(createdKlimaatSensor.getCode()).isEqualTo(SOME_SENSOR_CODE);
        assertThat(createdKlimaatSensor.getOmschrijving()).isNull();
    }

    @Test
    public void whenSaveThenOneKlimaatPerSensorSavedWithAverageSensorValues() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atTime(10, 15, 2);
        timeTravelTo(clock, currentDateTime);

        final Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = getRecentlyReceivedKlimaatPerSensorCode();

        final KlimaatSensor klimaatSensorBasement = aKlimaatSensor().withCode(SOME_SENSOR_CODE).build();

        when(klimaatSensorRepository.findFirstByCode(klimaatSensorBasement.getCode())).thenReturn(Optional.of(klimaatSensorBasement));

        final Klimaat recentValidKlimaat1 = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                      .withDatumtijd(currentDateTime.minusMinutes(10))
                                                      .withLuchtvochtigheid(new BigDecimal("25.00"))
                                                      .withTemperatuur(new BigDecimal("20.00"))
                                                      .build();
        final Klimaat recentValidKlimaat2 = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                      .withDatumtijd(currentDateTime.minusMinutes(5))
                                                      .withLuchtvochtigheid(new BigDecimal("75.00"))
                                                      .withTemperatuur(new BigDecimal("10.00"))
                                                      .build();
        final Klimaat recentInvalidKlimaat1 = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                        .withDatumtijd(currentDateTime.minusMinutes(4))
                                                        .withLuchtvochtigheid(ZERO)
                                                        .withTemperatuur(ZERO)
                                                        .build();
        final  Klimaat recentInvalidKlimaat2 = aKlimaat().withKlimaatSensor(klimaatSensorBasement)
                                                         .withDatumtijd(currentDateTime.minusMinutes(3))
                                                         .withLuchtvochtigheid(null)
                                                         .withTemperatuur(null)
                                                         .build();

        recentlyReceivedKlimaatsPerKlimaatSensorCode.put(klimaatSensorBasement.getCode(),
                                                         asList(recentValidKlimaat1, recentValidKlimaat2, recentInvalidKlimaat1, recentInvalidKlimaat2));

        klimaatService.save();

        verify(klimaatRepository).save(klimaatCaptor.capture());

        final Klimaat savedKlimaat = klimaatCaptor.getValue();
        assertThat(savedKlimaat.getDatumtijd()).isEqualTo(LocalDate.of(2016, JANUARY, 1).atTime(10, 15, 0));
        assertThat(savedKlimaat.getLuchtvochtigheid()).isEqualTo(new BigDecimal("50.0"));
        assertThat(savedKlimaat.getTemperatuur()).isEqualTo(new BigDecimal("15.00"));
    }

    @Test
    public void whenGetHighestTemperatureThenDelegatedToRepository() {
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakHighTemperatureDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(singletonList(day));
        when(klimaatRepository.earliestHighestTemperatureOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        final List<Klimaat> highest = klimaatService.getHighest(SOME_SENSOR_CODE, TEMPERATUUR, aPeriodWithToDate(from, to), limit);

        assertThat(highest).containsExactly(klimaat);
    }

    @Test
    public void whenGetLowestTemperatureThenDelegatedToRepository() {
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakLowTemperatureDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(singletonList(day));
        when(klimaatRepository.earliestLowestTemperatureOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        final List<Klimaat> lowest = klimaatService.getLowest(SOME_SENSOR_CODE, TEMPERATUUR, aPeriodWithToDate(from, to), limit);

        assertThat(lowest).containsExactly(klimaat);
    }

    @Test
    public void whenGetHighestHumidityThenDelegatedToRepository() {
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakHighHumidityDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(singletonList(day));
        when(klimaatRepository.earliestHighestHumidityOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        final List<Klimaat> highest = klimaatService.getHighest(SOME_SENSOR_CODE, LUCHTVOCHTIGHEID, aPeriodWithToDate(from, to), limit);

        assertThat(highest).containsExactly(klimaat);
    }

    @Test
    public void whenGetLowestHumidityThenDelegatedToRepository() {
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakLowHumidityDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(singletonList(day));
        when(klimaatRepository.earliestLowestHumidityOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        final List<Klimaat> lowest = klimaatService.getLowest(SOME_SENSOR_CODE, LUCHTVOCHTIGHEID, aPeriodWithToDate(from, to), limit);

        assertThat(lowest).containsExactly(klimaat);
    }

    @Test
    public void givenUnexpectedSensorTypewhenGetHighestThenException() {
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 10);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final SensorType unexpectedSensorType = null;
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> klimaatService.getLowest(SOME_SENSOR_CODE, unexpectedSensorType, aPeriodWithToDate(from, to), limit))
                .withMessage("Unexpected SensorType [null]");
    }

    @Test
    public void givenUnexpectedSensorTypewhenGetLowestThenException() {
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final SensorType unexpectedSensorType = null;
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> klimaatService.getHighest(SOME_SENSOR_CODE, unexpectedSensorType, aPeriodWithToDate(from, to), limit))
                .withMessage("Unexpected SensorType [null]");
    }

    @Test
    public void givenNoRecentlyReceivedKlimaatWhenGetMostRecentThenMostRecentIsNull() {
        final RealtimeKlimaat mostRecent = klimaatService.getMostRecent(SOME_SENSOR_CODE);

        assertThat(mostRecent).isNull();
    }

    @Test
    public void givenMultipleRecentlyReceivedKLimaatWhenGetMostRecentThenMostRecentIsReturned() {
        final LocalDate date = LocalDate.of(2016, SEPTEMBER, 1);
        timeTravelTo(clock, date.atStartOfDay());


        final Klimaat klimaat1 = aKlimaat().withDatumtijd(date.atTime(12, 14, 41)).build();
        final Klimaat klimaat2 = aKlimaat().withKlimaatSensor(aKlimaatSensor().withCode(SOME_SENSOR_CODE).build())
                                           .withDatumtijd(date.atTime(23, 26, 8))
                                           .withTemperatuur(new BigDecimal("23.7"))
                                           .withLuchtvochtigheid(new BigDecimal("45.7"))
                                           .build();
        final Klimaat klimaat3 = aKlimaat().withDatumtijd(date.atTime(2, 0, 45)).build();

        getRecentlyReceivedKlimaatPerSensorCode().put(SOME_SENSOR_CODE, asList(klimaat1, klimaat2, klimaat3));

        final RealtimeKlimaat mostRecent = klimaatService.getMostRecent(SOME_SENSOR_CODE);
        assertThat(mostRecent.getDatumtijd()).isEqualTo(klimaat2.getDatumtijd());
        assertThat(mostRecent.getTemperatuur()).isEqualTo(klimaat2.getTemperatuur());
        assertThat(mostRecent.getLuchtvochtigheid()).isEqualTo(klimaat2.getLuchtvochtigheid());
    }

    @Test
    public void whenGetAverageHumidityPerMonthInYearsThenAveragesReturned() {
        final LocalDate date = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, date.atStartOfDay());

        final int[] years = {2017, 2018};

        final BigDecimal averageHumidityInJune2017 = new BigDecimal("22.18");
        when(klimaatRepository.getAverageLuchtvochtigheid(SOME_SENSOR_CODE, LocalDate.of(2017, JUNE, 1).atStartOfDay(), LocalDate.of(2017, JULY, 1).atStartOfDay()))
                              .thenReturn(averageHumidityInJune2017);

        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(SOME_SENSOR_CODE, SensorType.LUCHTVOCHTIGHEID, years);

        assertThat(averagePerMonthInYears).hasSize(2);

        final List<GemiddeldeKlimaatPerMaand> averagesIn2017 = averagePerMonthInYears.get(0);
        assertThat(averagesIn2017).hasSize(12);
        for (final Month month : Month.values()) {
            assertThat(averagesIn2017.get(month.ordinal()).getMaand()).isEqualTo(LocalDate.of(2017, month, 1));
        }
        assertThat(averagesIn2017.get(JUNE.ordinal()).getGemiddelde()).isEqualTo(averageHumidityInJune2017);

        final List<GemiddeldeKlimaatPerMaand> averagesIn2018 = averagePerMonthInYears.get(1);
        assertThat(averagesIn2018).hasSize(12);
        for (final Month month : Month.values()) {
            assertThat(averagesIn2018.get(month.ordinal()).getMaand()).isEqualTo(LocalDate.of(2018, month, 1));
        }
    }

    @Test
    public void whenGetAverageTemperaturePerMonthInYearsThenAveragesReturned() {
        final LocalDate date = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, date.atStartOfDay());

        final int[] years = {2016};

        final BigDecimal averageTemperatureInJune2016 = new BigDecimal("22.18");
        when(klimaatRepository.getAverageTemperatuur(SOME_SENSOR_CODE, LocalDate.of(2016, JUNE, 1).atStartOfDay(), LocalDate.of(2016, JULY, 1).atStartOfDay()))
                              .thenReturn(averageTemperatureInJune2016);

        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(SOME_SENSOR_CODE, SensorType.TEMPERATUUR, years);

        assertThat(averagePerMonthInYears).hasSize(1);

        final List<GemiddeldeKlimaatPerMaand> averagesIn2016 = averagePerMonthInYears.get(0);
        assertThat(averagesIn2016).hasSize(12);
        for (final Month month : Month.values()) {
            assertThat(averagesIn2016.get(month.ordinal()).getMaand()).isEqualTo(LocalDate.of(2016, month, 1));
        }

        assertThat(averagesIn2016.get(JUNE.ordinal()).getGemiddelde()).isEqualTo(averageTemperatureInJune2016);
    }

    @Test
    public void givenInvalidSensortypeWhenGetAverageTemperaturePerMonthInYearsThenExceptionThrown() {
        final LocalDate date = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, date.atStartOfDay());

        final int[] someYears = {2015};

        final SensorType invalidSensorType = null;

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> klimaatService.getAveragePerMonthInYears(SOME_SENSOR_CODE, invalidSensorType, someYears))
            .withMessage("Unexpected SensorType [null]");
    }

    @Test
    public void whenGetInPeriodBeforeCurrentDateTimeThenRetrievedFromCache() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatService cachedProxy = mock(KlimaatService.class);
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", cachedProxy);

        final DatePeriod period = aPeriodWithToDate(currentDateTime.minusDays(1).toLocalDate(), currentDateTime.toLocalDate());

        final List<Klimaat> cachedKlimaats = asList(mock(Klimaat.class), mock(Klimaat.class));
        when(cachedProxy.getPotentiallyCachedAllInPeriod(eq(SOME_SENSOR_CODE), eq(period))).thenReturn(cachedKlimaats);

        assertThat(klimaatService.getInPeriod(SOME_SENSOR_CODE, period)).isSameAs(cachedKlimaats);
    }

    @Test
    public void whenGetInPeriodIncludingCurrentDateThenRetrievedFromRepository() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final DatePeriod period = aPeriodWithToDate(currentDateTime.toLocalDate(), currentDateTime.plusDays(1).toLocalDate());

        final List<Klimaat> klimaatsFromRepository = asList(mock(Klimaat.class), mock(Klimaat.class));
        when(klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(SOME_SENSOR_CODE, period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay().minusNanos(1)))
                              .thenReturn(klimaatsFromRepository);

        assertThat(klimaatService.getInPeriod(SOME_SENSOR_CODE, period)).isSameAs(klimaatsFromRepository);
    }

    @Test
    public void whenGetPotentiallyCachedAllInPeriodThenRetrievedFromRepository() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final DatePeriod period = aPeriodWithToDate(currentDateTime.toLocalDate(), currentDateTime.plusDays(1).toLocalDate());

        final List<Klimaat> klimaatsFromRepository = asList(mock(Klimaat.class), mock(Klimaat.class));
        when(klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(SOME_SENSOR_CODE, period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay().minusNanos(1)))
                .thenReturn(klimaatsFromRepository);

        assertThat(klimaatService.getPotentiallyCachedAllInPeriod(SOME_SENSOR_CODE, period)).isSameAs(klimaatsFromRepository);
    }

    @Test
    public void whenUpdateKlimaatSensorThenSavedByRepositoryAndReturned() {
        final KlimaatSensor klimaatSensor = mock(KlimaatSensor.class);
        final KlimaatSensor savedKlimaatSensor = mock(KlimaatSensor.class);
        when(klimaatSensorRepository.save(klimaatSensor)).thenReturn(savedKlimaatSensor);

        assertThat(klimaatService.update(klimaatSensor)).isSameAs(savedKlimaatSensor);
    }

    @Test
    public void whenDeleteKlimaatSensorThenDataAndSensorDeletedByRepositories() {
        final KlimaatSensor klimaatSensor = mock(KlimaatSensor.class);
        when(klimaatSensor.getCode()).thenReturn(SOME_SENSOR_CODE);

        klimaatService.delete(klimaatSensor);

        verify(klimaatRepository).deleteByKlimaatSensorCode(SOME_SENSOR_CODE);
        verify(klimaatSensorRepository).delete(klimaatSensor);
    }

    @Test
    public void givenRequestedYearIsInFutureWhenGetAveragePerMonthInYearsThenResultsAreEmpty() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();

        timeTravelTo(clock, currentDateTime);

        final KlimaatService cachedProxy = mock(KlimaatService.class);
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", cachedProxy);

        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(SOME_SENSOR_CODE, SensorType.TEMPERATUUR, new int[]{2018});
        assertThat(averagePerMonthInYears).hasSize(1);

        final List<GemiddeldeKlimaatPerMaand> monthsInOneYear = averagePerMonthInYears.get(0);
        assertThat(monthsInOneYear).hasSize(12);
        assertThat(monthsInOneYear).extracting(GemiddeldeKlimaatPerMaand::getGemiddelde).containsOnlyNulls();

        verifyZeroInteractions(klimaatRepository, cachedProxy);
    }

    @Test
    public void givenRequestedYearIsInPastWhenGetAveragePerMonthInYearsThenResultsRequestedFromRepository() {
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();

        timeTravelTo(clock, currentDateTime);

        final KlimaatService cachedProxy = mock(KlimaatService.class);
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", cachedProxy);

        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(SOME_SENSOR_CODE, SensorType.TEMPERATUUR, new int[]{2015});
        assertThat(averagePerMonthInYears).hasSize(1);

        final List<GemiddeldeKlimaatPerMaand> monthsInOneYear = averagePerMonthInYears.get(0);
        assertThat(monthsInOneYear).hasSize(12);

        verify(cachedProxy, times(12)).getPotentiallyCachedAverageInMonthOfYear(eq(SOME_SENSOR_CODE), any(), any());
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Klimaat>> getRecentlyReceivedKlimaatPerSensorCode() {
        return (Map<String, List<Klimaat>>) getField(klimaatService, "recentlyReceivedKlimaatsPerKlimaatSensorCode");
    }
}