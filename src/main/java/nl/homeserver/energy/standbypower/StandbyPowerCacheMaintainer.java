package nl.homeserver.energy.standbypower;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.DailyCacheMaintainer;
import nl.homeserver.cache.StartupCacheWarmer;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Stream;

import static nl.homeserver.CachingConfiguration.CACHE_NAME_STANDBY_POWER;

@Slf4j
@Component
@RequiredArgsConstructor
class StandbyPowerCacheMaintainer implements StartupCacheWarmer, DailyCacheMaintainer {

    private final StandbyPowerController standbyPowerController;
    private final CacheManager cacheManager;
    private final Clock clock;

    @Override
    public void warmupCacheOnStartup() {
        warmupCache();
    }

    @Override
    public void maintainCacheDaily() {
        cacheManager.getCache(CACHE_NAME_STANDBY_POWER).clear();
        warmupCache();
    }

    private void warmupCache() {
        final LocalDate today = LocalDate.now(clock);
        log.info("Warmup of cache standbyPower");
        Stream.of(today.getYear(), today.getYear() - 1)
              .sorted()
              .forEach(standbyPowerController::getStandbyPower);
    }
}
