package nl.homeserver.energie.opgenomenvermogen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.cache.CacheService;
import nl.homeserver.housekeeping.HousekeepingSchedule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpgenomenVermogenHousekeeping {

    private static final int NR_OF_ROWS_TO_KEEP_PER_MINUTE = 1;
    private static final int NR_OF_ROWS_TO_KEEP_PER_HOUR = NR_OF_ROWS_TO_KEEP_PER_MINUTE * 60;
    private static final int MAX_NR_OF_ROWS_PER_DAY = NR_OF_ROWS_TO_KEEP_PER_HOUR * 24;

    private static final int NUMBER_OF_MONTHS_TO_LOOK_BACK = 1;

    private final OpgenomenVermogenRepository opgenomenVermogenRepository;
    private final CacheService cacheService;
    private final Clock clock;

    @Scheduled(cron = HousekeepingSchedule.OPGENOMEN_VERMOGEN_CLEANUP)
    public void start() {
        log.info("Start housekeeping of OpgenomenVermogen");
        findDaysToCleanup().forEach(this::cleanup);
        cacheService.clear(OpgenomenVermogenService.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY);
        log.info("Finished housekeeping of OpgenomenVermogen");
    }

    private List<LocalDate> findDaysToCleanup() {
        final LocalDate today = LocalDate.now(clock);
        return opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(today.minusMonths(NUMBER_OF_MONTHS_TO_LOOK_BACK), today, MAX_NR_OF_ROWS_PER_DAY)
                .stream()
                .map(Date::toLocalDate)
                .toList();
    }

    private void cleanup(final LocalDate day) {
        log.info("Cleanup day {}", day);

        final List<OpgenomenVermogen> opgenomenVermogensOnDay = opgenomenVermogenRepository.getOpgenomenVermogen(day.atStartOfDay(), day.plusDays(1).atStartOfDay());

        final Map<Integer, List<OpgenomenVermogen>> opgenomenVermogensByHour = opgenomenVermogensOnDay.stream()
                                                                                                      .collect(groupingBy(opgenomenVermogen -> opgenomenVermogen.getDatumtijd().getHour()));

        opgenomenVermogensByHour.forEach(this::cleanupHour);
    }

    private void cleanupHour(final int hour, final List<OpgenomenVermogen> opgenomenVermogensInOneHour) {
        log.debug("Cleanup hour {}", hour);

        final Map<Integer, List<OpgenomenVermogen>> opgenomenVermogensByMinute = opgenomenVermogensInOneHour.stream()
                                                                                                            .collect(groupingBy(opgenomenVermogen -> opgenomenVermogen.getDatumtijd().getMinute()));

        final List<OpgenomenVermogen> opgenomenVermogensToKeep = opgenomenVermogensByMinute.values()
                                                                                           .stream()
                                                                                           .map(this::getOpgenomenVermogenToKeepInMinute)
                                                                                           .toList();

        final List<OpgenomenVermogen> opgenomenVermogensToDelete = opgenomenVermogensInOneHour.stream()
                                                                                        .filter(opgenomenVermogen -> !opgenomenVermogensToKeep.contains(opgenomenVermogen))
                                                                                        .toList();

        log(opgenomenVermogensToDelete, opgenomenVermogensToKeep);

        opgenomenVermogensToDelete.forEach(opgenomenVermogen -> opgenomenVermogenRepository.deleteById(opgenomenVermogen.getId()));
    }

    private void log(final List<OpgenomenVermogen> opgenomenVermogensToDelete, final List<OpgenomenVermogen> opgenomenVermogensToKeep) {
        if (log.isDebugEnabled()) {
            opgenomenVermogensToKeep.forEach(opgenomenVermogen -> log.debug("Keep: {}", opgenomenVermogen));
            opgenomenVermogensToDelete.forEach(opgenomenVermogen -> log.debug("Delete: {}", opgenomenVermogen));
        }
    }

    private OpgenomenVermogen getOpgenomenVermogenToKeepInMinute(final List<OpgenomenVermogen> opgenomenVermogenInOneMinute) {
        final Comparator<OpgenomenVermogen> byHighestWattThenDatumtijd = comparingInt(OpgenomenVermogen::getWatt).thenComparing(OpgenomenVermogen::getDatumtijd);
        return opgenomenVermogenInOneMinute.stream()
                                           .max(byHighestWattThenDatumtijd).orElse(null);
    }
}
