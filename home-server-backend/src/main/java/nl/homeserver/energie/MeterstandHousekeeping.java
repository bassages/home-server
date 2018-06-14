package nl.homeserver.energie;

import nl.homeserver.cache.CacheService;
import nl.homeserver.housekeeping.HousekeepingSchedule;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

@Service
public class MeterstandHousekeeping {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterstandHousekeeping.class);

    private static final int NR_OF_ROWS_TO_KEEP_PER_HOUR = 2;
    private static final int MAX_NR_OF_ROWS_PER_DAY = NR_OF_ROWS_TO_KEEP_PER_HOUR * 24;

    private final MeterstandRepository meterstandRepository;
    private final CacheService cacheService;
    private final Clock clock;

    public MeterstandHousekeeping(final MeterstandRepository meterstandRepository,
                                  final CacheService cacheService,
                                  final Clock clock) {
        this.meterstandRepository = meterstandRepository;
        this.cacheService = cacheService;
        this.clock = clock;
    }

    @Scheduled(cron = HousekeepingSchedule.METERSTAND)
    public void start() {
        findDaysToCleanup().forEach(this::cleanup);
        clearCachesThatUsesPossibleDeletedMeterstanden();
    }

    private List<LocalDate> findDaysToCleanup() {
        return meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(LocalDate.now(clock), MAX_NR_OF_ROWS_PER_DAY)
                                   .stream()
                                   .map(timestamp -> timestamp.toLocalDateTime().toLocalDate())
                                   .collect(toList());
    }

    private void cleanup(final LocalDate day) {
        final LocalDateTime start = day.atStartOfDay();
        final LocalDateTime end = start.plusDays(1).minusNanos(1);

        final List<Meterstand> meterstandenOnDay = meterstandRepository.findByDateTimeBetween(start, end);

        final Map<Integer, List<Meterstand>> meterstandenByHour = meterstandenOnDay.stream()
                                                                                  .collect(groupingBy(meterstand -> meterstand.getDateTime().getHour()));

        meterstandenByHour.values().forEach(this::cleanupMeterStandenInOneHour);
    }

    private void clearCachesThatUsesPossibleDeletedMeterstanden() {
        cacheService.clear(VerbruikKostenOverzichtService.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
        cacheService.clear(VerbruikKostenOverzichtService.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
    }

    private void cleanupMeterStandenInOneHour(final List<Meterstand> meterstandenInOneHour) {

        if (meterstandenInOneHour.size() > NR_OF_ROWS_TO_KEEP_PER_HOUR) {
            meterstandenInOneHour.sort(comparing(Meterstand::getDateTime));

            final Meterstand firstMeterstandInHour = meterstandenInOneHour.get(0);
            meterstandenInOneHour.remove(firstMeterstandInHour);

            final Meterstand lastMeterstandInHour = meterstandenInOneHour.get(meterstandenInOneHour.size() - 1);
            meterstandenInOneHour.remove(lastMeterstandInHour);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Keep first in hour {}: {}", firstMeterstandInHour.getDateTime().getHour(), ReflectionToStringBuilder.toString(firstMeterstandInHour, SHORT_PREFIX_STYLE));
                LOGGER.info("Keep last in hour {}: {}", lastMeterstandInHour.getDateTime().getHour(), ReflectionToStringBuilder.toString(lastMeterstandInHour, SHORT_PREFIX_STYLE));
                meterstandenInOneHour.forEach(meterstand -> LOGGER.info("Delete: {}", ReflectionToStringBuilder.toString(meterstand, SHORT_PREFIX_STYLE)));
            }

            meterstandRepository.deleteInBatch(meterstandenInOneHour);
        }
    }
}
