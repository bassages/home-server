package nl.homeserver.energie.opgenomenvermogen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.DailyCacheWarmer;
import nl.homeserver.cache.InitialCacheWarmer;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.concurrent.TimeUnit.MINUTES;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@Slf4j
@Component
@RequiredArgsConstructor
class OpgenomenVermogenCacheWarmer implements InitialCacheWarmer, DailyCacheWarmer {
    private static final long DEFAULT_SUB_PERIOD_LENGTH_IN_SECONDS = MINUTES.toMillis(3);

    private final OpgenomenVermogenController opgenomenVermogenController;
    private final Clock clock;

    @Override
    public void warmupInitialCache() {
        log.info("Warmup of cache opgenomenVermogenHistory");
        final LocalDate today = LocalDate.now(clock);
        aPeriodWithToDate(today.minusDays(14), today).getDays().forEach(this::warmupCacheForDay);

    }

    @Override
    public void warmupDailyCache() {
        log.info("Warmup of cache opgenomenVermogenHistory");
        final LocalDate yesterday = now(clock).minusDays(1);
        warmupCacheForDay(yesterday);
    }

    private void warmupCacheForDay(final LocalDate day) {
        opgenomenVermogenController.getOpgenomenVermogenHistory(
                day, day.plusDays(1), DEFAULT_SUB_PERIOD_LENGTH_IN_SECONDS);
    }
}
