package nl.homeserver.energy.standbypower;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.time.Clock;
import java.time.LocalDate;

import static java.time.Month.JUNE;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_STANDBY_POWER;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandbyPowerCacheMaintainerTest {

    @InjectMocks
    StandbyPowerCacheMaintainer standbyPowerCacheMaintainer;

    @Mock
    StandbyPowerController standbyPowerController;
    @Mock
    CacheManager cacheManager;
    @Mock
    Clock clock;

    @Test
    void whenWarmupCacheOnStartupThenStandbyPowerCacheWarmedUp() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        // when
        standbyPowerCacheMaintainer.warmupCacheOnStartup();

        // then
        verify(standbyPowerController).getStandbyPower(2017);
        verify(standbyPowerController).getStandbyPower(2016);
        verifyNoMoreInteractions(standbyPowerController);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void whenMaintainCacheDailyThenStandbyPowerCacheMaintained() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        final Cache cache = mock(Cache.class);
        when(cacheManager.getCache(CACHE_NAME_STANDBY_POWER)).thenReturn(cache);

        // when
        standbyPowerCacheMaintainer.maintainCacheDaily();

        // then
        verify(cache).clear();
        verify(standbyPowerController).getStandbyPower(2017);
        verify(standbyPowerController).getStandbyPower(2016);
        verifyNoMoreInteractions(standbyPowerController);
    }
}
