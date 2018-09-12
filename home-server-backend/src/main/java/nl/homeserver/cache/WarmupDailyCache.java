package nl.homeserver.cache;

import nl.homeserver.energie.EnergieController;
import nl.homeserver.energie.MeterstandController;
import nl.homeserver.energie.OpgenomenVermogenController;
import nl.homeserver.energie.StandbyPowerController;
import nl.homeserver.housekeeping.HousekeepingSchedule;
import nl.homeserver.klimaat.KlimaatController;
import nl.homeserver.klimaat.KlimaatSensor;
import nl.homeserver.klimaat.KlimaatService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class WarmupDailyCache {

    private static final Logger LOGGER = getLogger(WarmupDailyCache.class);

    private final OpgenomenVermogenController opgenomenVermogenController;
    private final EnergieController energieController;
    private final KlimaatController klimaatController;
    private final KlimaatService klimaatService;
    private final MeterstandController meterstandController;
    private final StandbyPowerController standbyPowerController;
    private final Clock clock;

    @Value("${warmupCache.daily}")
    private boolean warmupCacheDaily;

    public WarmupDailyCache(final OpgenomenVermogenController opgenomenVermogenController,
                            final EnergieController energieController,
                            final KlimaatController klimaatController,
                            final KlimaatService klimaatService,
                            final MeterstandController meterstandController,
                            final StandbyPowerController standbyPowerController,
                            final Clock clock) {
        this.opgenomenVermogenController = opgenomenVermogenController;
        this.energieController = energieController;
        this.klimaatController = klimaatController;
        this.klimaatService = klimaatService;
        this.meterstandController = meterstandController;
        this.standbyPowerController = standbyPowerController;
        this.clock = clock;
    }

    @Scheduled(cron = HousekeepingSchedule.WARMUP_CACHE)
    public void considerDailyWarmup() {
        if (warmupCacheDaily) {
            dailyWarmup();
        }
    }

    private void dailyWarmup() {
        LOGGER.info("Warming up cache...");
        warmupDailyEnergyCache();
        warmupDailyClimateCache();
        LOGGER.info("Warmup of cache completed");
    }

    private void warmupDailyEnergyCache() {
        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        LOGGER.info("Warmup of cache opgenomenVermogenHistory");
        opgenomenVermogenController.getOpgenomenVermogenHistory(yesterday, today, MINUTES.toMillis(3));

        LOGGER.info("Warmup of cache meterstandenPerDag");
        meterstandController.perDag(yesterday.with(firstDayOfMonth()), yesterday.with(lastDayOfMonth()));

        LOGGER.info("Warmup of cache verbruikPerUurOpDag");
        energieController.getVerbruikPerUurOpDag(yesterday);

        LOGGER.info("Warmup of cache verbruikPerDag");
        energieController.getVerbruikPerDag(yesterday.with(firstDayOfMonth()), yesterday.with(lastDayOfMonth()));

        LOGGER.info("Warmup of cache verbruikPerMaandInJaar");
        energieController.getVerbruikPerMaandInJaar(yesterday.getYear());

        LOGGER.info("Warmup of cache verbruikPerJaar");
        energieController.getVerbruikPerJaar();

        LOGGER.info("Warmup of cache standbyPower");
        Stream.of(today.getYear(), today.getYear() - 1).sorted().forEach(standbyPowerController::getStandbyPower);
    }

    private void warmupDailyClimateCache() {
        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        final List<KlimaatSensor> klimaatSensors = klimaatService.getAllKlimaatSensors();

        for (final KlimaatSensor klimaatSensor : klimaatSensors) {
            LOGGER.info("Warmup of cache klimaat history for sensor {}", klimaatSensor);
            klimaatController.findAllInPeriod(klimaatSensor.getCode(), yesterday, today);
        }
    }
}
