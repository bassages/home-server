package nl.homeserver.energy.opgenomenvermogen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.DailyCacheMaintainer;
import nl.homeserver.cache.StartupCacheWarmer;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;
import java.time.Clock;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.util.concurrent.TimeUnit.MINUTES;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@Slf4j
@Component
@RequiredArgsConstructor
class OpgenomenVermogenCacheMaintainer implements StartupCacheWarmer, DailyCacheMaintainer {
    private static final long DEFAULT_SUB_PERIOD_LENGTH_IN_SECONDS = MINUTES.toMillis(3);

    private final OpgenomenVermogenController opgenomenVermogenController;
    private final CacheManager cacheManager;
    private final Clock clock;

    @Override
    public void warmupCacheOnStartup() {
        log.info("Warmup of cache opgenomenVermogenHistory");
        final LocalDate today = LocalDate.now(clock);
        aPeriodWithToDate(today.minusDays(14), today).getDays().forEach(this::warmupCacheForDay);

    }

    @Override
    public void maintainCacheDaily() {
        log.info("Maintenance of cache %s".formatted(CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY));
        cacheManager.getCache(CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY).clear();

        final LocalDate yesterday = now(clock).minusDays(1);
        warmupCacheForDay(yesterday);
    }

    private void warmupCacheForDay(final LocalDate day) {
        opgenomenVermogenController.getOpgenomenVermogenHistory(
                day, day.plusDays(1), DEFAULT_SUB_PERIOD_LENGTH_IN_SECONDS);
    }
}
