package nl.homeserver.energy.verbruikkosten;

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
import static nl.homeserver.CachingConfiguration.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE;
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
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        // when
        verbruikKostenCacheMaintainer.warmupCacheOnStartup();

        // then
        verify(verbruikKostenController, times(13)).getVerbruikPerDag(
                fromDateCaptor.capture(), toDateCaptor.capture());

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
