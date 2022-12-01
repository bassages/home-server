package nl.homeserver.energie.meterstand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.DailyCacheWarmer;
import nl.homeserver.cache.StartupCacheWarmer;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;

import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.LongStream.rangeClosed;

@Slf4j
@Component
@RequiredArgsConstructor
class MeterstandCacheWarmer implements StartupCacheWarmer, DailyCacheWarmer {
    private static final Month[] MONTHS = Month.values();

    private final MeterstandController meterstandController;
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
    public void warmupCacheDaily() {
        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        log.info("Warmup of cache meterstandenPerDag");
        meterstandController.perDag(yesterday.with(firstDayOfMonth()), yesterday.with(lastDayOfMonth()));
    }
}
