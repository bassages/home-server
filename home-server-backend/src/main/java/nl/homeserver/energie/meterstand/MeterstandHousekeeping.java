package nl.homeserver.energie.meterstand;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.homeserver.cache.CacheService;
import nl.homeserver.energie.verbruikkosten.VerbruikKostenOverzichtService;
import nl.homeserver.housekeeping.HousekeepingSchedule;

@Service
@AllArgsConstructor
class MeterstandHousekeeping {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterstandHousekeeping.class);

    private static final int NR_OF_ROWS_TO_KEEP_PER_HOUR = 2;
    private static final int MAX_NR_OF_ROWS_PER_DAY = NR_OF_ROWS_TO_KEEP_PER_HOUR * 24;

    private static final int NUMBER_OF_MONTHS_TO_LOOK_BACK = 1;

    private final MeterstandRepository meterstandRepository;
    private final CacheService cacheService;
    private final Clock clock;

    @Scheduled(cron = HousekeepingSchedule.METERSTAND_CLEANUP)
    public void start() {
        LOGGER.info("Start housekeeping of Meterstand");
        findDaysToCleanup().forEach(this::cleanup);
        clearCachesThatUsesPossibleDeletedMeterstanden();
        LOGGER.info("Finished housekeeping of Meterstand");
    }

    private List<LocalDate> findDaysToCleanup() {
        final LocalDate today = LocalDate.now(clock);
        return meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(today.minusMonths(NUMBER_OF_MONTHS_TO_LOOK_BACK), today, MAX_NR_OF_ROWS_PER_DAY)
                                   .stream()
                                   .map(timestamp -> timestamp.toLocalDateTime().toLocalDate())
                                   .collect(toList());
    }

    private void cleanup(final LocalDate day) {
        LOGGER.info("Cleanup day {}", day);

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
        LOGGER.info("Cleanup hour {}", hour);

        if (meterstandenInOneHour.size() > NR_OF_ROWS_TO_KEEP_PER_HOUR) {
            meterstandenInOneHour.sort(comparing(Meterstand::getDateTime));

            final Meterstand firstMeterstandInHour = meterstandenInOneHour.get(0);
            meterstandenInOneHour.remove(firstMeterstandInHour);

            final Meterstand lastMeterstandInHour = meterstandenInOneHour.get(meterstandenInOneHour.size() - 1);
            meterstandenInOneHour.remove(lastMeterstandInHour);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Keep first in hour {}: {}", firstMeterstandInHour.getDateTime().getHour(), ReflectionToStringBuilder.toString(firstMeterstandInHour, SHORT_PREFIX_STYLE));
                LOGGER.debug("Keep last in hour {}: {}", lastMeterstandInHour.getDateTime().getHour(), ReflectionToStringBuilder.toString(lastMeterstandInHour, SHORT_PREFIX_STYLE));
                meterstandenInOneHour.forEach(meterstand -> LOGGER.debug("Delete: {}", ReflectionToStringBuilder.toString(meterstand, SHORT_PREFIX_STYLE)));
            }

            meterstandRepository.deleteInBatch(meterstandenInOneHour);
        }
    }
}
