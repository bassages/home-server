package nl.homeserver.energy.meterreading;

import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.cache.Cache;
import javax.cache.CacheManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeterstandCacheMaintainerTest {

    @InjectMocks
    MeterstandCacheMaintainer meterstandCacheMaintainer;

    @Mock
    MeterstandController meterstandController;
    @Mock
    CacheManager cacheManager;
    @Mock
    Clock clock;

    @Captor
    ArgumentCaptor<LocalDate> fromDateCaptor;
    @Captor
    ArgumentCaptor<LocalDate> toDateCaptor;

    @Test
    void whenWarmupCacheOnStartupThenMeterstandenPerDagMaintained() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        // when
        meterstandCacheMaintainer.warmupCacheOnStartup();

        // then
        verify(meterstandController, times(13)).perDag(fromDateCaptor.capture(), toDateCaptor.capture());

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2016, DECEMBER, 1),
                LocalDate.of(2017, JANUARY, 1),
                LocalDate.of(2017, FEBRUARY, 1),
                LocalDate.of(2017, MARCH, 1),
                LocalDate.of(2017, APRIL, 1),
                LocalDate.of(2017, MAY, 1),
                LocalDate.of(2017, JUNE, 1),
                LocalDate.of(2017, JULY, 1),
                LocalDate.of(2017, AUGUST, 1),
                LocalDate.of(2017, SEPTEMBER, 1),
                LocalDate.of(2017, OCTOBER, 1),
                LocalDate.of(2017, NOVEMBER, 1),
                LocalDate.of(2017, DECEMBER, 1)
        );

        assertThat(toDateCaptor.getAllValues()).containsExactly(
                YearMonth.of(2016, DECEMBER).atEndOfMonth(),
                YearMonth.of(2017, JANUARY).atEndOfMonth(),
                YearMonth.of(2017, FEBRUARY).atEndOfMonth(),
                YearMonth.of(2017, MARCH).atEndOfMonth(),
                YearMonth.of(2017, APRIL).atEndOfMonth(),
                YearMonth.of(2017, MAY).atEndOfMonth(),
                YearMonth.of(2017, JUNE).atEndOfMonth(),
                YearMonth.of(2017, JULY).atEndOfMonth(),
                YearMonth.of(2017, AUGUST).atEndOfMonth(),
                YearMonth.of(2017, SEPTEMBER).atEndOfMonth(),
                YearMonth.of(2017, OCTOBER).atEndOfMonth(),
                YearMonth.of(2017, NOVEMBER).atEndOfMonth(),
                YearMonth.of(2017, DECEMBER).atEndOfMonth()
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void whenMaintainCacheDailyThenMeterstandenPerDagMaintained() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        final Cache cache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG)).thenReturn(cache);

        // when
        meterstandCacheMaintainer.maintainCacheDaily();

        // then
        verify(cache).clear();
        verify(meterstandController).perDag(LocalDate.of(2017, DECEMBER, 1), LocalDate.of(2017, DECEMBER, 31));
    }
}
