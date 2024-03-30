package nl.homeserver.cache;

import lombok.extern.slf4j.Slf4j;
import nl.homeserver.housekeeping.HousekeepingSchedule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
class DailyCacheMaintenance {

    private final List<DailyCacheMaintainer> dailyCacheMaintainers;

    @Value("${home-server.cache.maintenance.daily}")
    private boolean isDailyCacheMaintenanceEnabled;

    DailyCacheMaintenance(final List<DailyCacheMaintainer> dailyCacheMaintainers) {
        this.dailyCacheMaintainers = dailyCacheMaintainers;
    }

    @Scheduled(cron = HousekeepingSchedule.WARMUP_CACHE)
    public void considerDailyWarmup() {
        if (isDailyCacheMaintenanceEnabled) {
            log.info("Maintaining cache start");
            dailyCacheMaintainers.forEach(DailyCacheMaintainer::maintainCacheDaily);
            log.info("Maintaining cache completed");
        }
    }
}
