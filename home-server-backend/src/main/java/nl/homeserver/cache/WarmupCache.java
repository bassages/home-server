package nl.homeserver.cache;

import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Comparator.reverseOrder;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.rangeClosed;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import nl.homeserver.energie.EnergieController;
import nl.homeserver.energie.MeterstandController;
import nl.homeserver.energie.OpgenomenVermogenController;
import nl.homeserver.klimaat.KlimaatController;
import nl.homeserver.klimaat.KlimaatSensor;
import nl.homeserver.klimaat.KlimaatService;

@Component
public class WarmupCache implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = getLogger(WarmupCache.class);

    private final OpgenomenVermogenController opgenomenVermogenController;
    private final EnergieController energieController;
    private final KlimaatController klimaatController;
    private final KlimaatService klimaatService;
    private final MeterstandController meterstandController;
    private final Clock clock;

    @Value("${warmupCache.on-application-start}")
    private boolean warmupCacheOnApplicationStart;

    public WarmupCache(final OpgenomenVermogenController opgenomenVermogenController,
                       final EnergieController energieController,
                       final KlimaatController klimaatController,
                       final KlimaatService klimaatService,
                       final MeterstandController meterstandController,
                       final Clock clock) {
        this.opgenomenVermogenController = opgenomenVermogenController;
        this.energieController = energieController;
        this.klimaatController = klimaatController;
        this.klimaatService = klimaatService;
        this.meterstandController = meterstandController;
        this.clock = clock;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (warmupCacheOnApplicationStart) {
            warmupEnergyCache();
            warmupClimateCache();
            LOGGER.info("Warmup of cache completed");
        }
    }

    private void warmupEnergyCache() {
        LocalDate today = now(clock);

        LOGGER.info("Warmup of cache opgenomenVermogenHistory");
        aPeriodWithToDate(today.minusDays(14), today).getDays()
                                                    .forEach(day -> opgenomenVermogenController.getOpgenomenVermogenHistory(day,
                                                                                                                            day.plusDays(1),
                                                                                                                            MINUTES.toMillis(3)));
        LOGGER.info("Warmup of cache meterstandenPerDag");
        rangeClosed(0, Month.values().length).boxed()
                .collect(toList())
                .stream()
                .sorted(reverseOrder())
                .forEach(monthsToSubtract -> meterstandController.perDag(today.minusMonths(monthsToSubtract).with(firstDayOfMonth()),
                                                                         today.minusMonths(monthsToSubtract).with(lastDayOfMonth())));

        LOGGER.info("Warmup of cache verbruikPerUurOpDag");
        aPeriodWithToDate(today.minusDays(14), today).getDays()
                                                    .forEach(energieController::getVerbruikPerUurOpDag);

        LOGGER.info("Warmup of cache verbruikPerDag");
        rangeClosed(0, Month.values().length).boxed()
                                             .collect(toList())
                                             .stream()
                                             .sorted(reverseOrder())
                                             .forEach(monthsToSubtract -> energieController.getVerbruikPerDag(today.minusMonths(monthsToSubtract).with(firstDayOfMonth()),
                                                                                                              today.minusMonths(monthsToSubtract).with(lastDayOfMonth())));
        LOGGER.info("Warmup of cache verbruikPerMaandInJaar");
        rangeClosed(0, 1).boxed().collect(toList())
                                 .stream()
                                 .sorted(reverseOrder())
                                 .forEach(yearsToSubTract -> energieController.getVerbruikPerMaandInJaar(today.minusYears(yearsToSubTract).getYear()));

        LOGGER.info("Warmup of cache verbruikPerJaar");
        energieController.getVerbruikPerJaar();
    }

    private void warmupClimateCache() {
        LocalDate today = now(clock);

        LOGGER.info("Warmup of cache klimaat");

        List<KlimaatSensor> klimaatSensors = klimaatService.getAllKlimaatSensors();

        for (KlimaatSensor klimaatSensor : klimaatSensors) {
            aPeriodWithToDate(today.minusDays(7), today).getDays()
                    .forEach(day -> klimaatController.findAllInPeriod(klimaatSensor.getCode(), day, day.plusDays(1)));

        }
    }
}
