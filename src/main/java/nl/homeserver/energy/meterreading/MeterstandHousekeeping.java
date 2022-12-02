package nl.homeserver.energy.meterreading;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.CacheService;
import nl.homeserver.energy.verbruikkosten.VerbruikKostenOverzichtService;
import nl.homeserver.housekeeping.HousekeepingSchedule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@RequiredArgsConstructor
class MeterstandHousekeeping {

    private static final int NR_OF_ROWS_TO_KEEP_PER_HOUR = 2;
    private static final int MAX_NR_OF_ROWS_PER_DAY = NR_OF_ROWS_TO_KEEP_PER_HOUR * 24;

    private static final int NUMBER_OF_MONTHS_TO_LOOK_BACK = 1;

    private final MeterstandRepository meterstandRepository;
    private final CacheService cacheService;
    private final Clock clock;

    @Scheduled(cron = HousekeepingSchedule.METERSTAND_CLEANUP)
    public void start() {
        log.info("Start housekeeping of Meterstand");
        findDaysToCleanup().forEach(this::cleanup);
        clearCachesThatUsesPossibleDeletedMeterstanden();
        log.info("Finished housekeeping of Meterstand");
    }

    private List<LocalDate> findDaysToCleanup() {
        final LocalDate today = LocalDate.now(clock);
        return meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(today.minusMonths(NUMBER_OF_MONTHS_TO_LOOK_BACK), today, MAX_NR_OF_ROWS_PER_DAY)
                                   .stream()
                                   .map(Date::toLocalDate)
                                   .toList();
    }

    private void cleanup(final LocalDate day) {
        log.info("Cleanup day {}", day);

        final LocalDateTime start = day.atStartOfDay();
        final LocalDateTime end = start.plusDays(1).minusNanos(1);

        final List<Meterstand> meterstandenOnDay = meterstandRepository.findByDateTimeBetween(start, end);

        final Map<Integer, List<Meterstand>> meterstandenByHour = meterstandenOnDay.stream()
                                                                                   .collect(groupingBy(meterstand -> meterstand.getDateTime().getHour()));

        meterstandenByHour.forEach(this::cleanupMeterStandenInOneHour);
    }

    private void clearCachesThatUsesPossibleDeletedMeterstanden() {
        cacheService.clear(VerbruikKostenOverzichtService.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
        cacheService.clear(VerbruikKostenOverzichtService.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
    }

    private void cleanupMeterStandenInOneHour(final int hour, final List<Meterstand> meterstandenInOneHour) {
        log.info("Cleanup hour {}", hour);

        if (meterstandenInOneHour.size() > NR_OF_ROWS_TO_KEEP_PER_HOUR) {
            meterstandenInOneHour.sort(comparing(Meterstand::getDateTime));

            final Meterstand firstMeterstandInHour = meterstandenInOneHour.get(0);
            meterstandenInOneHour.remove(firstMeterstandInHour);

            final Meterstand lastMeterstandInHour = meterstandenInOneHour.get(meterstandenInOneHour.size() - 1);
            meterstandenInOneHour.remove(lastMeterstandInHour);

            if (log.isDebugEnabled()) {
                log.debug("Keep first in hour {}: {}", firstMeterstandInHour.getDateTime().getHour(), firstMeterstandInHour);
                log.debug("Keep last in hour {}: {}", lastMeterstandInHour.getDateTime().getHour(), lastMeterstandInHour);
                meterstandenInOneHour.forEach(meterstand -> log.debug("Delete: {}", meterstand));
            }

            meterstandRepository.deleteAllInBatch(meterstandenInOneHour);
        }
    }
}
