package nl.homeserver.energy.meterreading;

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
import java.time.YearMonth;

import static java.time.Month.DECEMBER;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        timeTravelTo(clock, LocalDate.of(2017, 12, 30).atTime(13, 20));

        // when
        meterstandCacheMaintainer.warmupCacheOnStartup();

        // then
        verify(meterstandController, times(13)).perDag(fromDateCaptor.capture(), toDateCaptor.capture());

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2016, 12, 1),
                LocalDate.of(2017, 1, 1),
                LocalDate.of(2017, 2, 1),
                LocalDate.of(2017, 3, 1),
                LocalDate.of(2017, 4, 1),
                LocalDate.of(2017, 5, 1),
                LocalDate.of(2017, 6, 1),
                LocalDate.of(2017, 7, 1),
                LocalDate.of(2017, 8, 1),
                LocalDate.of(2017, 9, 1),
                LocalDate.of(2017, 10, 1),
                LocalDate.of(2017, 11, 1),
                LocalDate.of(2017, 12, 1)
        );

        assertThat(toDateCaptor.getAllValues()).containsExactly(
                YearMonth.of(2016, 12).atEndOfMonth(),
                YearMonth.of(2017, 1).atEndOfMonth(),
                YearMonth.of(2017, 2).atEndOfMonth(),
                YearMonth.of(2017, 3).atEndOfMonth(),
                YearMonth.of(2017, 4).atEndOfMonth(),
                YearMonth.of(2017, 5).atEndOfMonth(),
                YearMonth.of(2017, 6).atEndOfMonth(),
                YearMonth.of(2017, 7).atEndOfMonth(),
                YearMonth.of(2017, 8).atEndOfMonth(),
                YearMonth.of(2017, 9).atEndOfMonth(),
                YearMonth.of(2017, 10).atEndOfMonth(),
                YearMonth.of(2017, 11).atEndOfMonth(),
                YearMonth.of(2017, 12).atEndOfMonth()
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
