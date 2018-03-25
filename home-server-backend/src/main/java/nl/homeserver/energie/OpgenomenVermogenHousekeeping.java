package nl.homeserver.energie;

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
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.homeserver.HousekeepingSchedule;
import nl.homeserver.cache.CacheService;

@Service
public class OpgenomenVermogenHousekeeping {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpgenomenVermogenHousekeeping.class);

    private static final int NR_OF_PAST_DAYS_TO_CLEANUP = 3;

    private final OpgenomenVermogenRepository opgenomenVermogenRepository;
    private final CacheService cacheService;
    private final Clock clock;

    public OpgenomenVermogenHousekeeping(final OpgenomenVermogenRepository opgenomenVermogenRepository,
                                         final CacheService cacheService,
                                         final Clock clock) {
        this.opgenomenVermogenRepository = opgenomenVermogenRepository;
        this.cacheService = cacheService;
        this.clock = clock;
    }

    @Scheduled(cron = HousekeepingSchedule.OPGENOMEN_VERMOGEN)
    public void dailyCleanup() {
        LocalDate today = LocalDate.now(clock);
        IntStream.rangeClosed(1, NR_OF_PAST_DAYS_TO_CLEANUP)
                 .forEach(i -> cleanup(today.minusDays(i)));
        cacheService.clear(OpgenomenVermogenService.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY);
    }

    public void cleanup(LocalDate day) {
        List<OpgenomenVermogen> opgenomenVermogensOnDay = opgenomenVermogenRepository.getOpgenomenVermogen(day.atStartOfDay(), day.plusDays(1).atStartOfDay());

        Map<Integer, List<OpgenomenVermogen>> opgenomenVermogensByHour = opgenomenVermogensOnDay.stream()
                                                                                                .collect(groupingBy(opgenomenVermogen -> opgenomenVermogen.getDatumtijd().getHour()));

        opgenomenVermogensByHour.values().forEach(this::cleanupHour);
    }

    private void cleanupHour(List<OpgenomenVermogen> opgenomenVermogensInOneHour) {
        Map<Integer, List<OpgenomenVermogen>> opgenomenVermogensByMinute = opgenomenVermogensInOneHour.stream()
                                                                                                      .collect(groupingBy(opgenomenVermogen -> opgenomenVermogen.getDatumtijd().getMinute()));

        List<OpgenomenVermogen> opgenomenVermogensToKeep = opgenomenVermogensByMinute.values()
                                                                                     .stream()
                                                                                     .map(this::getOpgenomenVermogenToKeepInMinute)
                                                                                     .collect(toList());

        List<OpgenomenVermogen> opgenomenVermogensToDelete = opgenomenVermogensInOneHour.stream()
                                                                                        .filter(opgenomenVermogen -> !opgenomenVermogensToKeep.contains(opgenomenVermogen))
                                                                                        .collect(toList());

        log(opgenomenVermogensToDelete, opgenomenVermogensToKeep);

        opgenomenVermogenRepository.deleteInBatch(opgenomenVermogensToDelete);
    }

    private void log(List<OpgenomenVermogen> opgenomenVermogensToDelete, List<OpgenomenVermogen> opgenomenVermogensToKeep) {
        if (LOGGER.isInfoEnabled()) {
            opgenomenVermogensToKeep.forEach(opgenomenVermogen -> LOGGER.info("Keep: {}", ReflectionToStringBuilder.toString(opgenomenVermogen, SHORT_PREFIX_STYLE)));
            opgenomenVermogensToDelete.forEach(opgenomenVermogen -> LOGGER.info("Delete: {}", ReflectionToStringBuilder.toString(opgenomenVermogen, SHORT_PREFIX_STYLE)));
        }
    }

    private OpgenomenVermogen getOpgenomenVermogenToKeepInMinute(List<OpgenomenVermogen> opgenomenVermogenInOneMinute) {
        Comparator<OpgenomenVermogen> byHighestWattThenDatumtijd = comparingInt(OpgenomenVermogen::getWatt).thenComparing(comparing(OpgenomenVermogen::getDatumtijd));
        return opgenomenVermogenInOneMinute.stream()
                                           .max(byHighestWattThenDatumtijd).orElse(null);
    }
}
