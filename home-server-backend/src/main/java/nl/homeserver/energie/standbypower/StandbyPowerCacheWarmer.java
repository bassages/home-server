package nl.homeserver.energie.standbypower;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import nl.homeserver.cache.DailyCacheWarmer;
import nl.homeserver.cache.InitialCacheWarmer;

@Component
class StandbyPowerCacheWarmer implements InitialCacheWarmer, DailyCacheWarmer {

    private static final Logger LOGGER = getLogger(StandbyPowerCacheWarmer.class);

    private final StandbyPowerController standbyPowerController;
    private final Clock clock;

    StandbyPowerCacheWarmer(final StandbyPowerController standbyPowerController, final Clock clock) {
        this.standbyPowerController = standbyPowerController;
        this.clock = clock;
    }

    @Override
    public void warmupInitialCache() {
        warmupCache();
    }

    @Override
    public void warmupDailyCache() {
        warmupCache();
    }

    private void warmupCache() {
        final LocalDate today = LocalDate.now(clock);
        LOGGER.info("Warmup of cache standbyPower");
        Stream.of(today.getYear(), today.getYear() - 1)
              .sorted()
              .forEach(standbyPowerController::getStandbyPower);
    }
}
