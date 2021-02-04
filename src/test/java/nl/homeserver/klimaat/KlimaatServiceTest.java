package nl.homeserver.klimaat;

import nl.homeserver.DatePeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static java.time.Month.*;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static nl.homeserver.klimaat.SensorType.LUCHTVOCHTIGHEID;
import static nl.homeserver.klimaat.SensorType.TEMPERATUUR;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class KlimaatServiceTest {

    private static final String SOME_SENSOR_CODE = "someSensorCode";

    @InjectMocks
    KlimaatService klimaatService;

    @Mock
    KlimaatRepos klimaatRepository;
    @Mock
    Clock clock;

    @BeforeEach
    void setup() {
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", klimaatService);
    }

    @Test
    void whenGetHighestTemperatureThenDelegatedToRepository() {
        // given
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakHighTemperatureDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(List.of(day));
        when(klimaatRepository.earliestHighestTemperatureOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        // when
        final List<Klimaat> highest = klimaatService.getHighest(SOME_SENSOR_CODE,
                                                                TEMPERATUUR,
                                                                aPeriodWithToDate(from, to),
                                                                limit);

        // then
        assertThat(highest).containsExactly(klimaat);
    }

    @Test
    void whenGetLowestTemperatureThenDelegatedToRepository() {
        // given
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakLowTemperatureDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(List.of(day));
        when(klimaatRepository.earliestLowestTemperatureOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        // when
        final List<Klimaat> lowest = klimaatService.getLowest(SOME_SENSOR_CODE,
                                                              TEMPERATUUR,
                                                              aPeriodWithToDate(from, to),
                                                              limit);

        // then
        assertThat(lowest).containsExactly(klimaat);
    }

    @Test
    void whenGetHighestHumidityThenDelegatedToRepository() {
        // given
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakHighHumidityDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(List.of(day));
        when(klimaatRepository.earliestHighestHumidityOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        // when
        final List<Klimaat> highest = klimaatService.getHighest(SOME_SENSOR_CODE,
                                                                LUCHTVOCHTIGHEID,
                                                                aPeriodWithToDate(from, to), limit);

        // then
        assertThat(highest).containsExactly(klimaat);
    }

    @Test
    void whenGetLowestHumidityThenDelegatedToRepository() {
        // given
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakLowHumidityDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(List.of(day));
        when(klimaatRepository.earliestLowestHumidityOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        // when
        final List<Klimaat> lowest = klimaatService.getLowest(SOME_SENSOR_CODE,
                                                              LUCHTVOCHTIGHEID,
                                                              aPeriodWithToDate(from, to), limit);

        // then
        assertThat(lowest).containsExactly(klimaat);
    }

    @Test
    void whenGetAverageHumidityPerMonthInYearsThenAveragesReturned() {
        // given
        final LocalDate date = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, date.atStartOfDay());

        final int[] years = {2017, 2018};

        final BigDecimal averageHumidityInJune2017 = new BigDecimal("22.18");
        lenient().when(klimaatRepository.getAverageLuchtvochtigheid(SOME_SENSOR_CODE,
                                                          LocalDate.of(2017, JUNE, 1).atStartOfDay(),
                                                          LocalDate.of(2017, JULY, 1).atStartOfDay()))
                              .thenReturn(averageHumidityInJune2017);

        // when
        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(
                SOME_SENSOR_CODE, SensorType.LUCHTVOCHTIGHEID, years);

        // then
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
    void whenGetAverageTemperaturePerMonthInYearsThenAveragesReturned() {
        // given
        final LocalDate date = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, date.atStartOfDay());

        final int[] years = {2016};

        final BigDecimal averageTemperatureInJune2016 = new BigDecimal("22.18");
        lenient().when(klimaatRepository.getAverageTemperatuur(
                SOME_SENSOR_CODE, LocalDate.of(2016, JUNE, 1).atStartOfDay(),
                                  LocalDate.of(2016, JULY, 1).atStartOfDay()))
                              .thenReturn(averageTemperatureInJune2016);

        // when
        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(
                SOME_SENSOR_CODE, SensorType.TEMPERATUUR, years);

        // then
        assertThat(averagePerMonthInYears).hasSize(1);

        final List<GemiddeldeKlimaatPerMaand> averagesIn2016 = averagePerMonthInYears.get(0);
        assertThat(averagesIn2016).hasSize(12);
        for (final Month month : Month.values()) {
            assertThat(averagesIn2016.get(month.ordinal()).getMaand()).isEqualTo(LocalDate.of(2016, month, 1));
        }

        assertThat(averagesIn2016.get(JUNE.ordinal()).getGemiddelde()).isEqualTo(averageTemperatureInJune2016);
    }

    @Test
    void whenGetInPeriodBeforeCurrentDateTimeThenRetrievedFromCache() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final KlimaatService cachedProxy = mock(KlimaatService.class);
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", cachedProxy);

        final DatePeriod period = aPeriodWithToDate(
                currentDateTime.minusDays(1).toLocalDate(), currentDateTime.toLocalDate());

        final List<Klimaat> cachedKlimaats = List.of(mock(Klimaat.class), mock(Klimaat.class));
        when(cachedProxy.getPotentiallyCachedAllInPeriod(SOME_SENSOR_CODE, period)).thenReturn(cachedKlimaats);

        // when
        final List<Klimaat> inPeriod = klimaatService.getInPeriod(SOME_SENSOR_CODE, period);

        // then
        assertThat(inPeriod).isSameAs(cachedKlimaats);
    }

    @Test
    void whenGetInPeriodIncludingCurrentDateThenRetrievedFromRepository() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final DatePeriod period = aPeriodWithToDate(currentDateTime.toLocalDate(), currentDateTime.plusDays(1).toLocalDate());

        final List<Klimaat> klimaatsFromRepository = List.of(mock(Klimaat.class), mock(Klimaat.class));
        when(klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(
                SOME_SENSOR_CODE, period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay().minusNanos(1)))
                              .thenReturn(klimaatsFromRepository);

        // when
        final List<Klimaat> inPeriod = klimaatService.getInPeriod(SOME_SENSOR_CODE, period);

        // then
        assertThat(inPeriod).isSameAs(klimaatsFromRepository);
    }

    @Test
    void whenGetPotentiallyCachedAllInPeriodThenRetrievedFromRepository() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();

        final DatePeriod period = aPeriodWithToDate(currentDateTime.toLocalDate(), currentDateTime.plusDays(1).toLocalDate());

        final List<Klimaat> klimaatsFromRepository = List.of(mock(Klimaat.class), mock(Klimaat.class));
        when(klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(
                    SOME_SENSOR_CODE,
                    period.getFromDate().atStartOfDay(),
                    period.getToDate().atStartOfDay().minusNanos(1)))
                .thenReturn(klimaatsFromRepository);

        // when
        final List<Klimaat> potentiallyCachedAllInPeriod = klimaatService.getPotentiallyCachedAllInPeriod(SOME_SENSOR_CODE, period);

        // then
        assertThat(potentiallyCachedAllInPeriod).isSameAs(klimaatsFromRepository);
    }

    @Test
    void givenRequestedYearIsInFutureWhenGetAveragePerMonthInYearsThenResultsAreEmpty() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final int yearinFuture = currentDateTime.getYear() + 1;

        final KlimaatService cachedProxy = mock(KlimaatService.class);
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", cachedProxy);

        // when
        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(
                SOME_SENSOR_CODE, SensorType.TEMPERATUUR, new int[]{yearinFuture});

        // then
        assertThat(averagePerMonthInYears).hasSize(1);

        final List<GemiddeldeKlimaatPerMaand> monthsInOneYear = averagePerMonthInYears.get(0);
        assertThat(monthsInOneYear).hasSize(12);
        assertThat(monthsInOneYear).extracting(GemiddeldeKlimaatPerMaand::getGemiddelde).containsOnlyNulls();

        verifyNoMoreInteractions(klimaatRepository, cachedProxy);
    }

    @Test
    void givenRequestedYearIsInPastWhenGetAveragePerMonthInYearsThenResultsRequestedFromRepository() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final int yearInPast = currentDateTime.getYear() - 1;

        final KlimaatService cachedProxy = mock(KlimaatService.class);
        setField(klimaatService, "klimaatServiceProxyWithEnabledCaching", cachedProxy);

        // when
        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(
                SOME_SENSOR_CODE, SensorType.TEMPERATUUR, new int[]{yearInPast});

        // then
        assertThat(averagePerMonthInYears).hasSize(1);

        final List<GemiddeldeKlimaatPerMaand> monthsInOneYear = averagePerMonthInYears.get(0);
        assertThat(monthsInOneYear).hasSize(12);

        verify(cachedProxy, times(12)).getPotentiallyCachedAverageInMonthOfYear(
                eq(SOME_SENSOR_CODE), any(), any());
    }

    @Test
    void givenSomeKlimaatWhenSaveThenDelegatedToRepository() {
        // given
        final Klimaat someKlimaat = aKlimaat().build();

        // when
        klimaatService.save(someKlimaat);

        // then
        verify(klimaatRepository).save(someKlimaat);
    }

    @Test
    void whenDeleteByKlimaatSensorThenDelegatedByRepository() {
        // when
        klimaatService.deleteByKlimaatSensor(KlimaatSensorBuilder.aKlimaatSensor().withCode(SOME_SENSOR_CODE).build());

        // then
        verify(klimaatRepository).deleteByKlimaatSensorCode(SOME_SENSOR_CODE);
    }
}
