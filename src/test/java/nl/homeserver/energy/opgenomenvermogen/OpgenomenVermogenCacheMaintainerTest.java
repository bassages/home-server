package nl.homeserver.energy.opgenomenvermogen;

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

import static java.time.Month.DECEMBER;
import static java.util.concurrent.TimeUnit.MINUTES;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpgenomenVermogenCacheMaintainerTest {

    @InjectMocks
    OpgenomenVermogenCacheMaintainer opgenomenVermogenCacheMaintainer;

    @Mock
    OpgenomenVermogenController opgenomenVermogenController;
    @Mock
    CacheManager cacheManager;
    @Mock
    Clock clock;

    @Captor
    ArgumentCaptor<LocalDate> fromDateCaptor;
    @Captor
    ArgumentCaptor<LocalDate> toDateCaptor;

    @Test
    void whenWarmupCacheOnStartupThenOpgenomenVermogenHistoryWarmedUp() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        opgenomenVermogenCacheMaintainer.warmupCacheOnStartup();

        verify(opgenomenVermogenController, times(14))
                .getOpgenomenVermogenHistory(fromDateCaptor.capture(),
                                             toDateCaptor.capture(),
                                             eq(MINUTES.toMillis(3)));

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
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

        assertThat(toDateCaptor.getAllValues()).containsExactly(
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
                LocalDate.of(2017, DECEMBER, 29),
                LocalDate.of(2017, DECEMBER, 30)
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void whenMaintainCacheDailyThenOpgenomenVermogenHistoryMaintained() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        final Cache cache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY)).thenReturn(cache);

        // then
        opgenomenVermogenCacheMaintainer.maintainCacheDaily();

        // then
        verify(cache).clear();
        verify(opgenomenVermogenController).getOpgenomenVermogenHistory(
                LocalDate.of(2017, DECEMBER, 29),
                LocalDate.of(2017, DECEMBER, 30),
                MINUTES.toMillis(3));
    }
}
