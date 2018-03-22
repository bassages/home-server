package nl.homeserver.energie;

import static java.time.LocalDate.now;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.homeserver.cache.CacheService;

@Service
public class MeterstandHouseKeeping {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterstandHouseKeeping.class);

    private static final String TWO_AM = "0 0 2 * * *";
    private static final int NR_OF_PAST_DAYS_TO_CLEANUP = 3;

    private final MeterstandRepository meterstandRepository;
    private final CacheService cacheService;
    private final Clock clock;

    public MeterstandHouseKeeping(final MeterstandRepository meterstandRepository,
                                  final CacheService cacheService,
                                  final Clock clock) {
        this.meterstandRepository = meterstandRepository;
        this.cacheService = cacheService;
        this.clock = clock;
    }

    @Scheduled(cron = TWO_AM)
    public void dailyCleanup() {
        LocalDate today = now(clock);
        IntStream.rangeClosed(1, NR_OF_PAST_DAYS_TO_CLEANUP)
                 .forEach(i -> cleanup(today.minusDays(i)));
        clearCachesThatUsesPossibleDeletedMeterstanden();
    }

    private void cleanup(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        List<Meterstand> meterstandenOnDay = meterstandRepository.findByDateTimeBetween(start, end);

        Map<Integer, List<Meterstand>> meterstandenByHour = meterstandenOnDay.stream()
                                                                             .collect(groupingBy(meterstand -> meterstand.getDateTime().getHour()));

        meterstandenByHour.values().forEach(this::cleanupMeterStandenInOneHour);
    }

    private void clearCachesThatUsesPossibleDeletedMeterstanden() {
        cacheService.clear(VerbruikKostenOverzichtService.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
        cacheService.clear(VerbruikKostenOverzichtService.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
    }

    private void cleanupMeterStandenInOneHour(List<Meterstand> meterstandenInOneHour) {
        meterstandenInOneHour.sort(comparing(Meterstand::getDateTime));

        if (meterstandenInOneHour.size() >= 2) {
            Meterstand firstMeterstandInHour = meterstandenInOneHour.get(0);
            meterstandenInOneHour.remove(firstMeterstandInHour);

            Meterstand lastMeterstandInHour = meterstandenInOneHour.get(meterstandenInOneHour.size() - 1);
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
