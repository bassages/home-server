package nl.homeserver.cache;

import lombok.extern.slf4j.Slf4j;
import nl.homeserver.housekeeping.HousekeepingSchedule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
class WarmupCacheDaily {

    private final List<DailyCacheWarmer> dailyCacheWarmers;

    @Value("${home-server.cache.warmup.daily}")
    private boolean isWarmupCacheDailyEnabled;

    WarmupCacheDaily(final List<DailyCacheWarmer> dailyCacheWarmers) {
        this.dailyCacheWarmers = dailyCacheWarmers;
    }

    @Scheduled(cron = HousekeepingSchedule.WARMUP_CACHE)
    public void considerDailyWarmup() {
        if (isWarmupCacheDailyEnabled) {
            log.info("Warmup cache start");
            dailyCacheWarmers.forEach(DailyCacheWarmer::warmupCacheDaily);
            log.info("Warmup cache completed");
        }
    }
}
