package nl.homeserver.climate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.DailyCacheMaintainer;
import nl.homeserver.cache.StartupCacheWarmer;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_AVERAGE_CLIMATE_IN_MONTH;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_CLIMATE_IN_PERIOD;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@Slf4j
@Component
@RequiredArgsConstructor
class KlimaatCacheMaintainer implements StartupCacheWarmer, DailyCacheMaintainer {

    private final KlimaatController klimaatController;
    private final CacheManager cacheManager;
    private final Clock clock;

    @Override
    public void warmupCacheOnStartup() {
        final LocalDate today = now(clock);

        log.info("Warmup of cache klimaat");

        final List<KlimaatSensor> klimaatSensors = klimaatController.getAllKlimaatSensors();

        for (final KlimaatSensor klimaatSensor : klimaatSensors) {
            log.info("Warmup of cache klimaat history for sensor {}", klimaatSensor);
            aPeriodWithToDate(today.minusDays(7), today).getDays()
                                                        .forEach(day -> klimaatController.findAllInPeriod(klimaatSensor.getCode(), day, day.plusDays(1)));

            log.info("Warmup of cache klimaat averages for sensor {}", klimaatSensor);
            final int[] years = {today.getYear(), today.getYear() - 1, today.getYear() - 2};
            Stream.of(SensorType.values())
                  .forEach(sentortype -> klimaatController.getAverage(klimaatSensor.getCode(), sentortype.name(), years));
        }

    }

    @Override
    public void maintainCacheDaily() {
        cacheManager.getCache(CACHE_NAME_AVERAGE_CLIMATE_IN_MONTH).clear();
        cacheManager.getCache(CACHE_NAME_CLIMATE_IN_PERIOD).clear();

        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        final List<KlimaatSensor> klimaatSensors = klimaatController.getAllKlimaatSensors();

        for (final KlimaatSensor klimaatSensor : klimaatSensors) {
            log.info("Warmup of cache klimaat history for sensor {}", klimaatSensor);
            klimaatController.findAllInPeriod(klimaatSensor.getCode(), yesterday, today);
        }
    }
}
