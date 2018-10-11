package nl.homeserver.energie.opgenomenvermogen;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.homeserver.cache.CacheService;
import nl.homeserver.housekeeping.HousekeepingSchedule;

@Service
@AllArgsConstructor
public class OpgenomenVermogenHousekeeping {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpgenomenVermogenHousekeeping.class);

    private static final int NR_OF_ROWS_TO_KEEP_PER_MINUTE = 1;
    private static final int NR_OF_ROWS_TO_KEEP_PER_HOUR = NR_OF_ROWS_TO_KEEP_PER_MINUTE * 60;
    private static final int MAX_NR_OF_ROWS_PER_DAY = NR_OF_ROWS_TO_KEEP_PER_HOUR * 24;

    private static final int NUMBER_OF_MONTHS_TO_LOOK_BACK = 1;

    private final OpgenomenVermogenRepository opgenomenVermogenRepository;
    private final CacheService cacheService;
    private final Clock clock;

    @Scheduled(cron = HousekeepingSchedule.OPGENOMEN_VERMOGEN_CLEANUP)
    public void start() {
        LOGGER.info("Start housekeeping of OpgenomenVermogen");
        findDaysToCleanup().forEach(this::cleanup);
        cacheService.clear(OpgenomenVermogenService.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY);
        LOGGER.info("Finished housekeeping of OpgenomenVermogen");
    }

    private List<LocalDate> findDaysToCleanup() {
        final LocalDate today = LocalDate.now(clock);
        return opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(today.minusMonths(NUMBER_OF_MONTHS_TO_LOOK_BACK), today, MAX_NR_OF_ROWS_PER_DAY)
                .stream()
                .map(timestamp -> timestamp.toLocalDateTime().toLocalDate())
                .collect(toList());
    }

    private void cleanup(final LocalDate day) {
        LOGGER.info("Cleanup day {}", day);

        final List<OpgenomenVermogen> opgenomenVermogensOnDay = opgenomenVermogenRepository.getOpgenomenVermogen(day.atStartOfDay(), day.plusDays(1).atStartOfDay());

        final Map<Integer, List<OpgenomenVermogen>> opgenomenVermogensByHour = opgenomenVermogensOnDay.stream()
                                                                                                      .collect(groupingBy(opgenomenVermogen -> opgenomenVermogen.getDatumtijd().getHour()));

        opgenomenVermogensByHour.forEach(this::cleanupHour);
    }

    private void cleanupHour(final int hour, final List<OpgenomenVermogen> opgenomenVermogensInOneHour) {
        LOGGER.debug("Cleanup hour {}", hour);

        final Map<Integer, List<OpgenomenVermogen>> opgenomenVermogensByMinute = opgenomenVermogensInOneHour.stream()
                                                                                                            .collect(groupingBy(opgenomenVermogen -> opgenomenVermogen.getDatumtijd().getMinute()));

        final List<OpgenomenVermogen> opgenomenVermogensToKeep = opgenomenVermogensByMinute.values()
                                                                                           .stream()
                                                                                           .map(this::getOpgenomenVermogenToKeepInMinute)
                                                                                           .collect(toList());

        final List<OpgenomenVermogen> opgenomenVermogensToDelete = opgenomenVermogensInOneHour.stream()
                                                                                        .filter(opgenomenVermogen -> !opgenomenVermogensToKeep.contains(opgenomenVermogen))
                                                                                        .collect(toList());

        log(opgenomenVermogensToDelete, opgenomenVermogensToKeep);

        opgenomenVermogensToDelete.forEach(opgenomenVermogen -> opgenomenVermogenRepository.deleteById(opgenomenVermogen.getId()));
    }

    private void log(final List<OpgenomenVermogen> opgenomenVermogensToDelete, final List<OpgenomenVermogen> opgenomenVermogensToKeep) {
        if (LOGGER.isDebugEnabled()) {
            opgenomenVermogensToKeep.forEach(opgenomenVermogen -> LOGGER.debug("Keep: {}", ReflectionToStringBuilder.toString(opgenomenVermogen, SHORT_PREFIX_STYLE)));
            opgenomenVermogensToDelete.forEach(opgenomenVermogen -> LOGGER.debug("Delete: {}", ReflectionToStringBuilder.toString(opgenomenVermogen, SHORT_PREFIX_STYLE)));
        }
    }

    private OpgenomenVermogen getOpgenomenVermogenToKeepInMinute(final List<OpgenomenVermogen> opgenomenVermogenInOneMinute) {
        final Comparator<OpgenomenVermogen> byHighestWattThenDatumtijd = comparingInt(OpgenomenVermogen::getWatt).thenComparing(OpgenomenVermogen::getDatumtijd);
        return opgenomenVermogenInOneMinute.stream()
                                           .max(byHighestWattThenDatumtijd).orElse(null);
    }
}
