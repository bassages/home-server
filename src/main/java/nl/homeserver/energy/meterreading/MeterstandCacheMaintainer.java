package nl.homeserver.energy.meterreading;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.DailyCacheMaintainer;
import nl.homeserver.cache.StartupCacheWarmer;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;

import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.LongStream.rangeClosed;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG;

@Slf4j
@Component
@RequiredArgsConstructor
class MeterstandCacheMaintainer implements StartupCacheWarmer, DailyCacheMaintainer {
    private static final Month[] MONTHS = Month.values();

    private final MeterstandController meterstandController;
    private final CacheManager cacheManager;
    private final Clock clock;

    @Override
    public void warmupCacheOnStartup() {
        final LocalDate today = now(clock);

        log.info("Warmup of cache meterstandenPerDag");
        rangeClosed(0, MONTHS.length).boxed()
                                     .toList()
                                     .stream()
                                     .sorted(reverseOrder())
                                     .forEach(monthsToSubtract -> meterstandController.perDag(today.minusMonths(monthsToSubtract).with(firstDayOfMonth()),
                                                                                              today.minusMonths(monthsToSubtract).with(lastDayOfMonth())));
    }

    @Override
    public void maintainCacheDaily() {
        cacheManager.getCache(CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG).clear();

        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        log.info("Warmup of cache meterstandenPerDag");
        meterstandController.perDag(yesterday.with(firstDayOfMonth()), yesterday.with(lastDayOfMonth()));
    }
}
