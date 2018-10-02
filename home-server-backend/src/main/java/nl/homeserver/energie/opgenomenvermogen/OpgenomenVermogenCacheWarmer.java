package nl.homeserver.energie.opgenomenvermogen;

import static java.time.LocalDate.now;
import static java.util.concurrent.TimeUnit.MINUTES;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Clock;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import nl.homeserver.cache.DailyCacheWarmer;
import nl.homeserver.cache.InitialCacheWarmer;

@Component
class OpgenomenVermogenCacheWarmer implements InitialCacheWarmer, DailyCacheWarmer {
    private static final Logger LOGGER = getLogger(OpgenomenVermogenCacheWarmer.class);

    private final OpgenomenVermogenController opgenomenVermogenController;
    private final Clock clock;

    OpgenomenVermogenCacheWarmer(final OpgenomenVermogenController opgenomenVermogenController, final Clock clock) {
        this.opgenomenVermogenController = opgenomenVermogenController;
        this.clock = clock;
    }

    @Override
    public void warmupInitialCache() {
        final LocalDate today = LocalDate.now(clock);

        LOGGER.info("Warmup of cache opgenomenVermogenHistory");
        aPeriodWithToDate(today.minusDays(14), today).getDays()
                                                     .forEach(day -> opgenomenVermogenController.getOpgenomenVermogenHistory(day,
                                                                                                                             day.plusDays(1),
                                                                                                                             MINUTES.toMillis(3)));

    }

    @Override
    public void warmupDailyCache() {
        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        LOGGER.info("Warmup of cache opgenomenVermogenHistory");
        opgenomenVermogenController.getOpgenomenVermogenHistory(yesterday, today, MINUTES.toMillis(3));
    }
}
