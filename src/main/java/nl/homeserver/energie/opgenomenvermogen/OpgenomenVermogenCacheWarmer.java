package nl.homeserver.energie.opgenomenvermogen;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.DailyCacheWarmer;
import nl.homeserver.cache.InitialCacheWarmer;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.util.concurrent.TimeUnit.MINUTES;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@Slf4j
@Component
@AllArgsConstructor
class OpgenomenVermogenCacheWarmer implements InitialCacheWarmer, DailyCacheWarmer {
    private final OpgenomenVermogenController opgenomenVermogenController;
    private final Clock clock;

    @Override
    public void warmupInitialCache() {
        final LocalDate today = LocalDate.now(clock);

        log.info("Warmup of cache opgenomenVermogenHistory");
        aPeriodWithToDate(today.minusDays(14), today).getDays()
                                                     .forEach(day -> opgenomenVermogenController.getOpgenomenVermogenHistory(day,
                                                                                                                             day.plusDays(1),
                                                                                                                             MINUTES.toMillis(3)));

    }

    @Override
    public void warmupDailyCache() {
        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        log.info("Warmup of cache opgenomenVermogenHistory");
        opgenomenVermogenController.getOpgenomenVermogenHistory(yesterday, today, MINUTES.toMillis(3));
    }
}
