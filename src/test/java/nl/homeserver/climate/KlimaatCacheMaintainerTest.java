package nl.homeserver.climate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.JUNE;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_AVERAGE_CLIMATE_IN_MONTH;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_CLIMATE_IN_PERIOD;
import static nl.homeserver.climate.KlimaatSensor.aKlimaatSensor;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KlimaatCacheMaintainerTest {

    @InjectMocks
    KlimaatCacheMaintainer klimaatCacheMaintainer;

    @Mock
    KlimaatController klimaatController;
    @Mock
    CacheManager cacheManager;
    @Mock
    Clock clock;

    @Captor
    ArgumentCaptor<LocalDate> fromDateCaptor;
    @Captor
    ArgumentCaptor<LocalDate> toDateCaptor;

    @Test
    void whenWarmupCacheOnStartupThenClimatePerDayWarmedup() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        final String sensorCode = "SOME_NICE_CODE";
        final KlimaatSensor klimaatSensor = aKlimaatSensor().code(sensorCode).build();
        when(klimaatController.getAllKlimaatSensors()).thenReturn(List.of(klimaatSensor));

        klimaatCacheMaintainer.warmupCacheOnStartup();

        verify(klimaatController, times(7)).findAllInPeriod(eq(sensorCode),
                fromDateCaptor.capture(), toDateCaptor.capture());

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 23),
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29)
        );

        assertThat(toDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29),
                LocalDate.of(2017, DECEMBER, 30)
        );
    }

    @Test
    void whenWarmupCacheOnStartupThenClimateAveragesWarmedup() {
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        final String sensorCode = "SOME_NICE_CODE";

        when(klimaatController.getAllKlimaatSensors())
                .thenReturn(List.of(aKlimaatSensor().code(sensorCode).build()));

        klimaatCacheMaintainer.warmupCacheOnStartup();

        verify(klimaatController).getAverage(sensorCode,
                                             SensorType.TEMPERATUUR.name(),
                                             new int[]{2017, 2016, 2015});
        verify(klimaatController).getAverage(sensorCode,
                                             SensorType.LUCHTVOCHTIGHEID.name(),
                                             new int[]{2017, 2016, 2015});
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void whenMaintainCacheDailyThenClimatePerDayMaintained() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        final Cache climateInPeriodCache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_CLIMATE_IN_PERIOD)).thenReturn(climateInPeriodCache);
        final Cache averageClimateInMonthCache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_AVERAGE_CLIMATE_IN_MONTH)).thenReturn(averageClimateInMonthCache);

        final String sensorCode = "SOME_FANCY_SENSOR";
        final KlimaatSensor klimaatSensor = aKlimaatSensor().code(sensorCode).build();
        when(klimaatController.getAllKlimaatSensors()).thenReturn(List.of(klimaatSensor));

        // when
        klimaatCacheMaintainer.maintainCacheDaily();

        // then
        verify(climateInPeriodCache).clear();
        verify(averageClimateInMonthCache).clear();
        verify(klimaatController).findAllInPeriod(sensorCode,
                LocalDate.of(2017, DECEMBER, 29), LocalDate.of(2017, DECEMBER, 30));
    }
}
