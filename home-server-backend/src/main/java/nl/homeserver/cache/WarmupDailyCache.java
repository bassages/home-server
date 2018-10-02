package nl.homeserver.cache;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import nl.homeserver.housekeeping.HousekeepingSchedule;

@Component
class WarmupDailyCache {

    private static final Logger LOGGER = getLogger(WarmupDailyCache.class);

    private final List<DailyCacheWarmer> dailyCacheWarmers;

    @Value("${cache.warmup.daily}")
    private boolean warmupCacheDaily;

    WarmupDailyCache(final List<DailyCacheWarmer> dailyCacheWarmers) {
        this.dailyCacheWarmers = dailyCacheWarmers;
    }

    @Scheduled(cron = HousekeepingSchedule.WARMUP_CACHE)
    public void considerDailyWarmup() {
        if (warmupCacheDaily) {
            LOGGER.info("Warmup cache start");
            dailyCacheWarmers.forEach(DailyCacheWarmer::warmupDailyCache);
            LOGGER.info("Warmup cache completed");
        }
    }
}
