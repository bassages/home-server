package nl.homeserver.energie.meterstand;

import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.rangeClosed;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import nl.homeserver.cache.DailyCacheWarmer;
import nl.homeserver.cache.InitialCacheWarmer;

@Component
@AllArgsConstructor
class MeterstandCacheWarmer implements InitialCacheWarmer, DailyCacheWarmer {

    private static final Logger LOGGER = getLogger(MeterstandCacheWarmer.class);

    private static final Month[] MONTHS = Month.values();

    private final MeterstandController meterstandController;
    private final Clock clock;

    @Override
    public void warmupInitialCache() {
        final LocalDate today = now(clock);

        LOGGER.info("Warmup of cache meterstandenPerDag");
        rangeClosed(0, MONTHS.length).boxed()
                                     .collect(toList())
                                     .stream()
                                     .sorted(reverseOrder())
                                     .forEach(monthsToSubtract -> meterstandController.perDag(today.minusMonths(monthsToSubtract).with(firstDayOfMonth()),
                                                                                              today.minusMonths(monthsToSubtract).with(lastDayOfMonth())));
    }

    @Override
    public void warmupDailyCache() {
        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        LOGGER.info("Warmup of cache meterstandenPerDag");
        meterstandController.perDag(yesterday.with(firstDayOfMonth()), yesterday.with(lastDayOfMonth()));
    }
}
