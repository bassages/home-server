package nl.homeserver.energy.verbruikkosten;

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
import static nl.homeserver.CachingConfiguration.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@Slf4j
@Component
@RequiredArgsConstructor
class VerbruikKostenCacheMaintainer implements StartupCacheWarmer, DailyCacheMaintainer {

    private static final Month[] ALL_MONTHS = Month.values();

    private final VerbruikKostenController verbruikKostenController;
    private final CacheManager cacheManager;
    private final Clock clock;

    @Override
    public void warmupCacheOnStartup() {
        final LocalDate today = now(clock);
        warmupVerbruikPerUurOpDag(today);
        warmupVerbruikPerDag(today);
        warmupVerbruikPerMaandInJaar(today);
        warmupVerbruikPerJaar();
    }

    private void warmupVerbruikPerUurOpDag(final LocalDate today) {
        log.info("Warmup of cache verbruikPerUurOpDag");
        aPeriodWithToDate(today.minusDays(14), today).getDays()
                                                     .forEach(verbruikKostenController::getVerbruikPerUurOpDag);
    }

    private void warmupVerbruikPerDag(final LocalDate today) {
        log.info("Warmup of cache verbruikPerDag");
        rangeClosed(0, ALL_MONTHS.length).boxed()
                                     .toList()
                                     .stream()
                                     .sorted(reverseOrder())
                                     .forEach(monthsToSubtract -> verbruikKostenController.getVerbruikPerDag(today.minusMonths(monthsToSubtract).with(firstDayOfMonth()),
                                                                                                             today.minusMonths(monthsToSubtract).with(lastDayOfMonth())));
    }

    private void warmupVerbruikPerMaandInJaar(final LocalDate today) {
        log.info("Warmup of cache verbruikPerMaandInJaar");
        rangeClosed(0, 1).boxed()
                         .toList()
                         .stream()
                         .sorted(reverseOrder())
                         .forEach(yearsToSubTract -> verbruikKostenController.getVerbruikPerMaandInJaar(today.minusYears(yearsToSubTract).getYear()));
    }

    @Override
    public void maintainCacheDaily() {
        cacheManager.getCache(CACHE_NAME_GAS_VERBRUIK_IN_PERIODE).clear();
        cacheManager.getCache(CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE).clear();

        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        log.info("Warmup of cache verbruikPerUurOpDag");
        verbruikKostenController.getVerbruikPerUurOpDag(yesterday);

        log.info("Warmup of cache verbruikPerDag");
        verbruikKostenController.getVerbruikPerDag(yesterday.with(firstDayOfMonth()), yesterday.with(lastDayOfMonth()));

        log.info("Warmup of cache verbruikPerMaandInJaar");
        verbruikKostenController.getVerbruikPerMaandInJaar(yesterday.getYear());

        warmupVerbruikPerJaar();
    }

    private void warmupVerbruikPerJaar() {
        log.info("Warmup of caches verbruikPerJaar");
        verbruikKostenController.getVerbruikPerJaar();
    }
}
