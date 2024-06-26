package nl.homeserver.energy.verbruikkosten;

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
import static java.time.Month.JANUARY;
import static nl.homeserver.CachingConfiguration.*;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerbruikKostenCacheMaintainerTest {

    @InjectMocks
    VerbruikKostenCacheMaintainer verbruikKostenCacheMaintainer;

    @Mock
    VerbruikKostenController verbruikKostenController;
    @Mock
    CacheManager cacheManager;
    @Mock
    Clock clock;

    @Captor
    ArgumentCaptor<LocalDate> fromDateCaptor;
    @Captor
    ArgumentCaptor<LocalDate> toDateCaptor;
    @Captor
    ArgumentCaptor<LocalDate> dateCaptor;
    @Captor
    ArgumentCaptor<Integer> yearCaptor;

    @Test
    void whenWarmupCacheOnStartupThenVerbruikPerUurOpDagWarmedUp() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        // when
        verbruikKostenCacheMaintainer.warmupCacheOnStartup();

        // then
        verify(verbruikKostenController, times(14)).getVerbruikPerUurOpDag(dateCaptor.capture());

        assertThat(dateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 16),
                LocalDate.of(2017, DECEMBER, 17),
                LocalDate.of(2017, DECEMBER, 18),
                LocalDate.of(2017, DECEMBER, 19),
                LocalDate.of(2017, DECEMBER, 20),
                LocalDate.of(2017, DECEMBER, 21),
                LocalDate.of(2017, DECEMBER, 22),
                LocalDate.of(2017, DECEMBER, 23),
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29)
        );
    }

    @Test
    void whenWarmupCacheOnStartupThenVerbruikPerDagWarmedUp() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, 12, 30).atTime(13, 20));

        // when
        verbruikKostenCacheMaintainer.warmupCacheOnStartup();

        // then
        verify(verbruikKostenController, times(13)).getVerbruikPerDag(
                fromDateCaptor.capture(), toDateCaptor.capture());

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

    @Test
    void whenWarmupCacheOnStartupThenVerbruikPerMaandInJaarWarmedUp() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        // when
        verbruikKostenCacheMaintainer.warmupCacheOnStartup();

        // then
        verify(verbruikKostenController, times(2)).getVerbruikPerMaandInJaar(yearCaptor.capture());
        assertThat(yearCaptor.getAllValues()).containsExactly(2016, 2017);
    }

    @Test
    void whenWarmupCacheOnStartupThenVerbruikPerJaarWarmedUp() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        // when
        verbruikKostenCacheMaintainer.warmupCacheOnStartup();

        // then
        verify(verbruikKostenController).getVerbruikPerJaar();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void whenWarmupCacheDailyThenVerbruikPerUurOpDagWarmedUpForYesterday() {
        // given
        final LocalDate today = LocalDate.of(2017, DECEMBER, 30);
        timeTravelTo(clock, today.atTime(13, 20));

        final Cache gasCache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_GAS_VERBRUIK_IN_PERIODE)).thenReturn(gasCache);
        final Cache stroomCache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE)).thenReturn(stroomCache);

        // when
        verbruikKostenCacheMaintainer.maintainCacheDaily();

        // then
        verify(gasCache).clear();
        verify(stroomCache).clear();
        verify(verbruikKostenController).getVerbruikPerUurOpDag(today.minusDays(1));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void whenMaintainCacheDailyThenVerbruikPerDagMaintainedForCurrentMonth() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        final Cache stroomCache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE)).thenReturn(stroomCache);
        final Cache gasCache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_GAS_VERBRUIK_IN_PERIODE)).thenReturn(gasCache);

        // when
        verbruikKostenCacheMaintainer.maintainCacheDaily();

        // then
        verify(stroomCache).clear();
        verify(gasCache).clear();
        verify(verbruikKostenController).getVerbruikPerDag(
                LocalDate.of(2017, DECEMBER, 1),
                LocalDate.of(2017, DECEMBER, 31));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void whenMaintainCacheDailyThenVerbruikPerMaandInJaarMaintainedForYesterdaysYear() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, JANUARY, 1).atTime(13, 20));

        final Cache stroomCache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE)).thenReturn(stroomCache);
        final Cache gasCache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_GAS_VERBRUIK_IN_PERIODE)).thenReturn(gasCache);

        // when
        verbruikKostenCacheMaintainer.maintainCacheDaily();

        // then
        verify(stroomCache).clear();
        verify(gasCache).clear();
        verify(verbruikKostenController).getVerbruikPerMaandInJaar(2016);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void whenMaintainCacheDailyThenVerbruikPerJaarMaintained() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));
        final Cache stroomCache = mock(Cache.class);

        when(cacheManager.getCache(CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE)).thenReturn(stroomCache);
        final Cache gasCache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_GAS_VERBRUIK_IN_PERIODE)).thenReturn(gasCache);

        // when
        verbruikKostenCacheMaintainer.maintainCacheDaily();

        // then
        verify(stroomCache).clear();
        verify(gasCache).clear();
        verify(verbruikKostenController).getVerbruikPerJaar();
    }
}
