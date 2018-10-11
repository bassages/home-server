package nl.homeserver.energie.verbruikkosten;

import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.rangeClosed;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
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
class VerbruikKostenCacheWarmer implements InitialCacheWarmer, DailyCacheWarmer {

    private static final Logger LOGGER = getLogger(VerbruikKostenCacheWarmer.class);

    private static final Month[] MONTHS = Month.values();

    private final VerbruikKostenController verbruikKostenController;
    private final Clock clock;

    @Override
    public void warmupInitialCache() {
        final LocalDate today = now(clock);
        warmupVerbruikPerUurOpDag(today);
        warmupVerbruikPerDag(today);
        warmupVerbruikPerMaandInJaar(today);
        warmupVerbruikPerJaar();
    }

    private void warmupVerbruikPerUurOpDag(final LocalDate today) {
        LOGGER.info("Warmup of cache verbruikPerUurOpDag");
        aPeriodWithToDate(today.minusDays(14), today).getDays()
                                                     .forEach(verbruikKostenController::getVerbruikPerUurOpDag);
    }

    private void warmupVerbruikPerDag(final LocalDate today) {
        LOGGER.info("Warmup of cache verbruikPerDag");
        rangeClosed(0, MONTHS.length).boxed()
                                     .collect(toList())
                                     .stream()
                                     .sorted(reverseOrder())
                                     .forEach(monthsToSubtract -> verbruikKostenController.getVerbruikPerDag(today.minusMonths(monthsToSubtract).with(firstDayOfMonth()),
                                                                                                             today.minusMonths(monthsToSubtract).with(lastDayOfMonth())));
    }

    private void warmupVerbruikPerMaandInJaar(final LocalDate today) {
        LOGGER.info("Warmup of cache verbruikPerMaandInJaar");
        rangeClosed(0, 1).boxed().collect(toList())
                         .stream()
                         .sorted(reverseOrder())
                         .forEach(yearsToSubTract -> verbruikKostenController.getVerbruikPerMaandInJaar(today.minusYears(yearsToSubTract).getYear()));
    }

    private void warmupVerbruikPerJaar() {
        LOGGER.info("Warmup of cache verbruikPerJaar");
        verbruikKostenController.getVerbruikPerJaar();
    }

    @Override
    public void warmupDailyCache() {
        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        LOGGER.info("Warmup of cache verbruikPerUurOpDag");
        verbruikKostenController.getVerbruikPerUurOpDag(yesterday);

        LOGGER.info("Warmup of cache verbruikPerDag");
        verbruikKostenController.getVerbruikPerDag(yesterday.with(firstDayOfMonth()), yesterday.with(lastDayOfMonth()));

        LOGGER.info("Warmup of cache verbruikPerMaandInJaar");
        verbruikKostenController.getVerbruikPerMaandInJaar(yesterday.getYear());

        warmupVerbruikPerJaar();
    }
}
