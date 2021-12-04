package nl.homeserver.klimaat;

import nl.homeserver.DatePeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.*;
import java.util.List;

import static java.math.BigDecimal.ZERO;
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
    KlimaatServiceHelper klimaatServiceHelper;
    @Mock
    KlimaatRepos klimaatRepository;
    @Mock
    Clock clock;

    @Test
    void whenGetHighestTemperatureThenRetrievedFromRepository() {
        // given
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakHighTemperatureDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(List.of(day));
        when(klimaatRepository.earliestHighestTemperatureOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        // when
        final List<Klimaat> highest = klimaatService.getHighest(SOME_SENSOR_CODE, TEMPERATUUR,
                aPeriodWithToDate(from, to), limit);

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
        final List<Klimaat> lowest = klimaatService.getLowest(SOME_SENSOR_CODE, TEMPERATUUR,
                aPeriodWithToDate(from, to), limit);

        // then
        assertThat(lowest).containsExactly(klimaat);
    }

    @Test
    void whenGetHighestHumidityThenRetrievedFromRepository() {
        // given
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakHighHumidityDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(List.of(day));
        when(klimaatRepository.earliestHighestHumidityOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        // when
        final List<Klimaat> highest = klimaatService.getHighest(SOME_SENSOR_CODE, LUCHTVOCHTIGHEID,
                aPeriodWithToDate(from, to), limit);

        // then
        assertThat(highest).containsExactly(klimaat);
    }

    @Test
    void whenGetLowestHumidityThenRetrievedFromRepository() {
        // given
        final LocalDate from = LocalDate.of(2016, SEPTEMBER, 1);
        final LocalDate to = from.plusDays(1);
        final int limit = 100;

        final Klimaat klimaat = aKlimaat().withDatumtijd(from.atStartOfDay()).build();

        final Date day = Date.valueOf(from);
        when(klimaatRepository.getPeakLowHumidityDates(SOME_SENSOR_CODE, from, to, limit)).thenReturn(List.of(day));
        when(klimaatRepository.earliestLowestHumidityOnDay(SOME_SENSOR_CODE, from)).thenReturn(klimaat);

        // when
        final List<Klimaat> lowest = klimaatService.getLowest(SOME_SENSOR_CODE, LUCHTVOCHTIGHEID,
                aPeriodWithToDate(from, to), limit);

        // then
        assertThat(lowest).containsExactly(klimaat);
    }

    // java:S5961: Test methods should not contain too many assertions
    @SuppressWarnings("java:S5961")
    @Test
    void whenGetAverageHumidityPerMonthInPastYearsThenPotentiallyCachedAveragesReturned() {
        // given
        final LocalDate date = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, date.atStartOfDay());

        final SensorType sensorType = LUCHTVOCHTIGHEID;
        when(klimaatServiceHelper.getPotentiallyCachedAverageInMonthOfYear(
                eq(SOME_SENSOR_CODE), eq(sensorType), any(YearMonth.class)))
            .thenAnswer(invocation -> {
                final YearMonth yearMonth = invocation.getArgument(2, YearMonth.class);
                final LocalDate month = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
                return new GemiddeldeKlimaatPerMaand(month, ZERO);
            });

        // when
        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(
                SOME_SENSOR_CODE, sensorType, 2017, 2018);

        // then
        assertThat(averagePerMonthInYears)
            .hasSize(2)
            .satisfiesExactly(
                year2017 -> assertThat(year2017)
                    .hasSize(12)
                    .satisfiesExactly(
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 1, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 2, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 3, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 4, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 5, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 6, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 7, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 8, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 9, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 10, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 11, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2017, 12, 1))),
                year2018 -> assertThat(year2018)
                    .hasSize(12)
                    .satisfiesExactly(
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 1, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 2, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 3, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 4, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 5, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 6, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 7, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 8, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 9, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 10, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 11, 1)),
                        gemiddeldeKlimaatPerMaand -> assertThat(gemiddeldeKlimaatPerMaand.getMaand()).isEqualTo(LocalDate.of(2018, 12, 1))));
    }

    @Test
    void whenGetInPeriodBeforeCurrentDateTimeThenRetrievedFromCache() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final DatePeriod period = aPeriodWithToDate(
                currentDateTime.minusDays(1).toLocalDate(), currentDateTime.toLocalDate());

        final List<Klimaat> cachedKlimaats = List.of(mock(Klimaat.class), mock(Klimaat.class));
        when(klimaatServiceHelper.getPotentiallyCachedAllInPeriod(SOME_SENSOR_CODE, period)).thenReturn(cachedKlimaats);

        // when
        final List<Klimaat> inPeriod = klimaatService.getInPeriod(SOME_SENSOR_CODE, period);

        // then
        assertThat(inPeriod).isSameAs(cachedKlimaats);
    }

    @Test
    void whenGetInPeriodIncludingCurrentDateThenNotCachedKlimaatsAreRetrieved() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 13).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final DatePeriod period = aPeriodWithToDate(currentDateTime.toLocalDate(), currentDateTime.plusDays(1).toLocalDate());

        final List<Klimaat> klimaats = List.of(mock(Klimaat.class), mock(Klimaat.class));
        when(klimaatServiceHelper.getNotCachedAllInPeriod(SOME_SENSOR_CODE, period)).thenReturn(klimaats);

        // when
        final List<Klimaat> inPeriod = klimaatService.getInPeriod(SOME_SENSOR_CODE, period);

        // then
        assertThat(inPeriod).isSameAs(klimaats);
    }

    @Test
    void givenRequestedYearIsInFutureWhenGetAveragePerMonthInYearsThenResultsAreEmpty() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final int yearInFuture = currentDateTime.getYear() + 1;

        // when
        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(
                SOME_SENSOR_CODE, SensorType.TEMPERATUUR, yearInFuture);

        // then
        assertThat(averagePerMonthInYears).hasSize(1);

        final List<GemiddeldeKlimaatPerMaand> monthsInOneYear = averagePerMonthInYears.get(0);
        assertThat(monthsInOneYear).hasSize(12);
        assertThat(monthsInOneYear).extracting(GemiddeldeKlimaatPerMaand::getGemiddelde).containsOnlyNulls();

        verifyNoMoreInteractions(klimaatRepository, klimaatServiceHelper);
    }

    @Test
    void givenRequestedYearIsInPastWhenGetAveragePerMonthInYearsThenResultsRequestedFromCache() {
        // given
        final LocalDateTime currentDateTime = LocalDate.of(2016, JANUARY, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final int yearInPast = currentDateTime.getYear() - 1;

        // when
        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(
                SOME_SENSOR_CODE, SensorType.TEMPERATUUR, yearInPast);

        // then
        assertThat(averagePerMonthInYears).hasSize(1);

        final List<GemiddeldeKlimaatPerMaand> monthsInOneYear = averagePerMonthInYears.get(0);
        assertThat(monthsInOneYear).hasSize(12);

        verify(klimaatServiceHelper, times(12)).getPotentiallyCachedAverageInMonthOfYear(
                eq(SOME_SENSOR_CODE), any(), any());
    }

    @Test
    void givenRequestedYearIsCurrentYearWhenGetAveragePerMonthInYearsThenCachedAndNotCachedAveragesAreRetrieved() {
        // given
        final int currentYear = 2016;
        final Month currentMonth = MAY;
        final LocalDateTime currentDateTime = LocalDate.of(currentYear, currentMonth, 1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final SensorType sensorType = TEMPERATUUR;

        // when
        final List<List<GemiddeldeKlimaatPerMaand>> averagePerMonthInYears = klimaatService.getAveragePerMonthInYears(
                SOME_SENSOR_CODE, sensorType, currentYear);

        // then
        assertThat(averagePerMonthInYears).hasSize(1);

        final List<GemiddeldeKlimaatPerMaand> monthsInOneYear = averagePerMonthInYears.get(0);
        assertThat(monthsInOneYear).hasSize(12);

        final InOrder inOrder = inOrder(klimaatServiceHelper);

        inOrder.verify(klimaatServiceHelper).getPotentiallyCachedAverageInMonthOfYear(
                SOME_SENSOR_CODE, sensorType, YearMonth.of(currentYear, 1));
        inOrder.verify(klimaatServiceHelper).getPotentiallyCachedAverageInMonthOfYear(
                SOME_SENSOR_CODE, sensorType, YearMonth.of(currentYear, 2));
        inOrder.verify(klimaatServiceHelper).getPotentiallyCachedAverageInMonthOfYear(
                SOME_SENSOR_CODE, sensorType, YearMonth.of(currentYear, 3));
        inOrder.verify(klimaatServiceHelper).getPotentiallyCachedAverageInMonthOfYear(
                SOME_SENSOR_CODE, sensorType, YearMonth.of(currentYear, 4));

        inOrder.verify(klimaatServiceHelper).getNotCachedAverageInMonthOfYear(
                SOME_SENSOR_CODE, sensorType, YearMonth.of(currentYear, currentMonth));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void givenSomeKlimaatWhenSaveThenSavedByRepository() {
        // given
        final Klimaat someKlimaat = aKlimaat().build();

        // when
        klimaatService.save(someKlimaat);

        // then
        verify(klimaatRepository).save(someKlimaat);
    }

    @Test
    void whenDeleteByKlimaatSensorThenDeletedByRepository() {
        // when
        klimaatService.deleteByKlimaatSensor(KlimaatSensorBuilder.aKlimaatSensor().withCode(SOME_SENSOR_CODE).build());

        // then
        verify(klimaatRepository).deleteByKlimaatSensorCode(SOME_SENSOR_CODE);
    }
}
