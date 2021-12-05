package nl.homeserver.klimaat;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.DailyCacheWarmer;
import nl.homeserver.cache.InitialCacheWarmer;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@Slf4j
@Component
@AllArgsConstructor
class KlimaatCacheWarmer implements InitialCacheWarmer, DailyCacheWarmer {

    private final KlimaatController klimaatController;
    private final Clock clock;

    @Override
    public void warmupInitialCache() {
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
    public void warmupDailyCache() {
        final LocalDate today = now(clock);
        final LocalDate yesterday = today.minusDays(1);

        final List<KlimaatSensor> klimaatSensors = klimaatController.getAllKlimaatSensors();

        for (final KlimaatSensor klimaatSensor : klimaatSensors) {
            log.info("Warmup of cache klimaat history for sensor {}", klimaatSensor);
            klimaatController.findAllInPeriod(klimaatSensor.getCode(), yesterday, today);
        }
    }
}
