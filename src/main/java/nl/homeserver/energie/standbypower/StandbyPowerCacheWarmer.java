package nl.homeserver.energie.standbypower;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.DailyCacheWarmer;
import nl.homeserver.cache.InitialCacheWarmer;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Stream;

@Slf4j
@Component
@AllArgsConstructor
class StandbyPowerCacheWarmer implements InitialCacheWarmer, DailyCacheWarmer {

    private final StandbyPowerController standbyPowerController;
    private final Clock clock;

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
        log.info("Warmup of cache standbyPower");
        Stream.of(today.getYear(), today.getYear() - 1)
              .sorted()
              .forEach(standbyPowerController::getStandbyPower);
    }
}
