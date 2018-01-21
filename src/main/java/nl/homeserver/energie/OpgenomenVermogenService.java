package nl.homeserver.energie;

import static java.time.LocalDateTime.from;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.DateTimeUtil.toMillisSinceEpoch;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.cache.CacheService;

@Service
public class OpgenomenVermogenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpgenomenVermogenService.class);

    private static final int NR_OF_PAST_DAYS_TO_CLEANUP = 3;
    public static final String CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY = "opgenomenVermogenHistory";

    private static final String ONE_AM = "0 0 1 * * *";

    public static final String TOPIC = "/topic/opgenomen-vermogen";

    private final CacheService cacheService;
    private final OpgenomenVermogenRepository opgenomenVermogenRepository;
    private final Clock clock;
    private final SimpMessagingTemplate messagingTemplate;

    public OpgenomenVermogenService(OpgenomenVermogenRepository opgenomenVermogenRepository, CacheService cacheService, Clock clock,
            SimpMessagingTemplate messagingTemplate) {

        this.cacheService = cacheService;
        this.opgenomenVermogenRepository = opgenomenVermogenRepository;
        this.clock = clock;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(cron = ONE_AM)
    public void dailyCleanup() {
        LocalDate today = LocalDate.now(clock);
        IntStream.rangeClosed(1, NR_OF_PAST_DAYS_TO_CLEANUP)
                 .forEach(i -> cleanup(today.minusDays(i)));
        cacheService.clear(CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY);
    }

    public OpgenomenVermogen save(OpgenomenVermogen opgenomenVermogen) {
        OpgenomenVermogen savedOpgenomenVermogen = opgenomenVermogenRepository.save(opgenomenVermogen);
        messagingTemplate.convertAndSend(TOPIC, savedOpgenomenVermogen);
        return savedOpgenomenVermogen;
    }

    public OpgenomenVermogen getMostRecent() {
        return opgenomenVermogenRepository.getMeestRecente();
    }

    @Cacheable(cacheNames = CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY)
    public List<OpgenomenVermogen> getPotentiallyCachedHistory(DatePeriod period, Duration subPeriodDuration) {
        return getHistory(period, subPeriodDuration);
    }

    public List<OpgenomenVermogen> getHistory(DatePeriod period, Duration subPeriodDuration) {
        DateTimePeriod dateTimePeriod = period.toDateTimePeriod();

        List<OpgenomenVermogen> opgenomenVermogenInPeriod = opgenomenVermogenRepository.getOpgenomenVermogen(
                dateTimePeriod.getFromDateTime(), dateTimePeriod.getToDateTime());

        long subPeriodLengthInMillis = subPeriodDuration.getSeconds() * 1000;
        long nrOfSubPeriodsInPeriod = (toMillisSinceEpoch(dateTimePeriod.getToDateTime()) - toMillisSinceEpoch(dateTimePeriod.getFromDateTime())) / subPeriodLengthInMillis;

        return LongStream.rangeClosed(0, nrOfSubPeriodsInPeriod)
                         .boxed()
                         .map(periodNumber -> this.toSubPeriod(dateTimePeriod.getStartDateTime(), periodNumber, subPeriodDuration))
                         .map(subPeriod -> this.getMaxOpgenomenVermogenInPeriode(opgenomenVermogenInPeriod, subPeriod))
                         .collect(toList());
    }

    private DateTimePeriod toSubPeriod(LocalDateTime from, long periodNumber, Duration subPeriodDuration) {
        Duration durationUntilStartOfSubPeriod = subPeriodDuration.multipliedBy(periodNumber);
        LocalDateTime subFrom = from(durationUntilStartOfSubPeriod.addTo(from));
        LocalDateTime subTo = from(subPeriodDuration.addTo(subFrom));
        return aPeriodWithToDateTime(subFrom, subTo);
    }

    private OpgenomenVermogen getMaxOpgenomenVermogenInPeriode(List<OpgenomenVermogen> opgenomenVermogens, DateTimePeriod period) {
        return opgenomenVermogens.stream()
                                 .filter(opgenomenVermogen -> period.isWithinPeriod(opgenomenVermogen.getDatumtijd()))
                                 .max(comparingInt(OpgenomenVermogen::getWatt))
                                 .map(o -> this.mapToOpgenomenVermogen(o, period))
                                 .orElse(this.mapToEmptyOpgenomenVermogen(period.getFromDateTime()));
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(OpgenomenVermogen opgenomenVermogen, DateTimePeriod period) {
        OpgenomenVermogen result = new OpgenomenVermogen();
        result.setTariefIndicator(opgenomenVermogen.getTariefIndicator());
        result.setDatumtijd(period.getFromDateTime());
        result.setWatt(opgenomenVermogen.getWatt());
        return result;
    }

    private OpgenomenVermogen mapToEmptyOpgenomenVermogen(LocalDateTime datumtijd) {
        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.ONBEKEND);
        opgenomenVermogen.setWatt(0);
        return opgenomenVermogen;
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

        opgenomenVermogensInOneHour.removeAll(opgenomenVermogensToKeep);

        opgenomenVermogensToKeep.forEach(opgenomenVermogen -> LOGGER.info("Keep: {}", ReflectionToStringBuilder.toString(opgenomenVermogen, SHORT_PREFIX_STYLE)));
        opgenomenVermogensInOneHour.forEach(opgenomenVermogen -> LOGGER.info("Delete: {}", ReflectionToStringBuilder.toString(opgenomenVermogen, SHORT_PREFIX_STYLE)));

        if (isNotEmpty(opgenomenVermogensInOneHour)) {
            opgenomenVermogenRepository.deleteInBatch(opgenomenVermogensInOneHour);
        }
    }

    private OpgenomenVermogen getOpgenomenVermogenToKeepInMinute(List<OpgenomenVermogen> opgenomenVermogenInOneMinute) {
        Comparator<OpgenomenVermogen> order = comparingInt(OpgenomenVermogen::getWatt).thenComparing(comparing(OpgenomenVermogen::getDatumtijd));
        return opgenomenVermogenInOneMinute.stream().max(order).get();
    }
}
