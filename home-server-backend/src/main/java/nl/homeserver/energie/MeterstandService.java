package nl.homeserver.energie;

import static java.time.LocalDate.now;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.homeserver.DatePeriod;
import nl.homeserver.cache.CacheService;

@Service
public class MeterstandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterstandService.class);

    private static final String CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG = "meestRecenteMeterstandOpDag";

    private static final String TWO_AM = "0 0 2 * * *";
    private static final int NR_OF_PAST_DAYS_TO_CLEANUP = 3;

    public static final String TOPIC = "/topic/meterstand";

    @Autowired
    private MeterstandService meterstandServiceProxyWithEnabledCaching; // Needed to make use of use caching annotations

    private final MeterstandRepository meterstandRepository;
    private final CacheService cacheService;
    private final Clock clock;
    private final SimpMessagingTemplate messagingTemplate;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Meterstand> mostRecentlySavedMeterstand = Optional.empty();

    public MeterstandService(MeterstandRepository meterstandRepository, CacheService cacheService, Clock clock, SimpMessagingTemplate messagingTemplate) {
        this.meterstandRepository = meterstandRepository;
        this.cacheService = cacheService;
        this.clock = clock;
        this.messagingTemplate = messagingTemplate;
    }

    public Meterstand save(Meterstand meterstand) {
        Meterstand savedMeterStand = meterstandRepository.save(meterstand);
        mostRecentlySavedMeterstand = Optional.of(savedMeterStand);
        messagingTemplate.convertAndSend(TOPIC, meterstand);
        return savedMeterStand;
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

    public Meterstand getMostRecent() {
        return mostRecentlySavedMeterstand.orElseGet(meterstandRepository::getMostRecent);
    }

    public Meterstand getOldest() {
        return meterstandRepository.getOldest();
    }

    public Meterstand getOldestOfToday() {
        LocalDate today = now(clock);

        LocalDateTime van = today.atStartOfDay();
        LocalDateTime totEnMet = today.atStartOfDay().plusDays(1).minusNanos(1);

        Meterstand oudsteStroomStandOpDag = meterstandRepository.getOldestInPeriod(van, totEnMet);

        if (oudsteStroomStandOpDag != null) {
            // Gas is registered once every hour, in the hour AFTER it actually is used. Compensate for that hour
            Meterstand oudsteGasStandOpDag = meterstandRepository.getOldestInPeriod(van.plusHours(1), totEnMet.plusHours(1));

            if (oudsteGasStandOpDag != null) {
                oudsteStroomStandOpDag.setGas(oudsteGasStandOpDag.getGas());
            }
        }
        return oudsteStroomStandOpDag;
    }

    public List<MeterstandOpDag> getPerDag(DatePeriod period) {
        return period.getDays().stream()
                               .map(this::getMeterstandOpDag)
                               .collect(toList());
    }

    private MeterstandOpDag getMeterstandOpDag(LocalDate day) {
        return new MeterstandOpDag(day, getMeesteRecenteMeterstandOpDag(day));
    }

    private Meterstand getMeesteRecenteMeterstandOpDag(LocalDate day) {
        LocalDate today = now(clock);
        if (day.isAfter(today)) {
            return null;
        } else if (day.isEqual(today)) {
            return getNonCachedMeestRecenteMeterstandOpDag(day);
        } else {
            return meterstandServiceProxyWithEnabledCaching.getPotentiallyCachedMeestRecenteMeterstandOpDag(day);
        }
    }

    @Cacheable(cacheNames = CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG)
    public Meterstand getPotentiallyCachedMeestRecenteMeterstandOpDag(LocalDate day) {
        return getNonCachedMeestRecenteMeterstandOpDag(day);
    }

    private Meterstand getNonCachedMeestRecenteMeterstandOpDag(LocalDate day) {
        LocalDateTime van = day.atStartOfDay();
        LocalDateTime totEnMet = day.atStartOfDay().plusDays(1).minusNanos(1);
        return meterstandRepository.getMostRecentInPeriod(van, totEnMet);
    }
}