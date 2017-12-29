package nl.wiegman.home.energie;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static nl.wiegman.home.DateTimeUtil.getDaysInPeriod;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpoch;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpochAtStartOfDay;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_HOUR;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DateTimePeriod;
import nl.wiegman.home.cache.CacheService;

@Service
public class MeterstandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterstandService.class);

    private static final String CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG = "meestRecenteMeterstandOpDag";

    private static final String TWO_AM = "0 0 2 * * *";
    private static final int NR_OF_PAST_DAYS_TO_CLEANUP = 3;

    @Autowired
    private MeterstandService meterstandServiceProxyWithEnabledCaching; // Needed to make use of use caching annotations

    private final MeterstandRepository meterstandRepository;
    private final CacheService cacheService;
    private final Clock clock;

    private Meterstand mostRecentlySavedMeterstand = null;

    @Autowired
    public MeterstandService(MeterstandRepository meterstandRepository, CacheService cacheService, Clock clock) {
        this.meterstandRepository = meterstandRepository;
        this.cacheService = cacheService;
        this.clock = clock;
    }

    public Meterstand save(Meterstand meterstand) {
        Meterstand savedMeterStand = meterstandRepository.save(meterstand);
        mostRecentlySavedMeterstand = savedMeterStand;
        return savedMeterStand;
    }

    @Scheduled(cron = TWO_AM)
    public void dailyCleanup() {
        LocalDate today = LocalDate.now(clock);
        IntStream.rangeClosed(1, NR_OF_PAST_DAYS_TO_CLEANUP)
                 .forEach(i -> cleanup(today.minusDays(i)));
        clearCachesThatUsesPossibleDeletedMeterstanden();
    }

    private void cleanup(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        List<Meterstand> meterstandenOnDay = meterstandRepository.findByDatumtijdBetween(toMillisSinceEpoch(start), toMillisSinceEpoch(end));

        Map<Integer, List<Meterstand>> meterstandenByHour = meterstandenOnDay.stream()
                .collect(groupingBy(item -> item.getDatumtijdAsLocalDateTime().getHour()));

        meterstandenByHour.values().forEach(this::cleanupMeterStandenInOneHour);
    }

    private void clearCachesThatUsesPossibleDeletedMeterstanden() {
        cacheService.clear(VerbruikService.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
        cacheService.clear(VerbruikService.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
    }

    private void cleanupMeterStandenInOneHour(List<Meterstand> meterstandenInOneHour) {
        meterstandenInOneHour.sort(comparing(Meterstand::getDatumtijd));

        if (meterstandenInOneHour.size() >= 2) {

            Meterstand firstMeterstandInHour = meterstandenInOneHour.get(0);
            LOGGER.info("Keep first: {} - {}", firstMeterstandInHour.getDatumtijdAsLocalDateTime(), ReflectionToStringBuilder.toString(firstMeterstandInHour, SHORT_PREFIX_STYLE));
            meterstandenInOneHour.remove(firstMeterstandInHour);

            Meterstand lastMeterstandInHour = meterstandenInOneHour.get(meterstandenInOneHour.size() - 1);
            LOGGER.info("Keep last: {} - {}", lastMeterstandInHour.getDatumtijdAsLocalDateTime(), ReflectionToStringBuilder.toString(lastMeterstandInHour, SHORT_PREFIX_STYLE));
            meterstandenInOneHour.remove(lastMeterstandInHour);

            if (isNotEmpty(meterstandenInOneHour)) {
                meterstandenInOneHour.forEach(meterstand -> LOGGER.info("Delete: {}", ReflectionToStringBuilder.toString(meterstand, SHORT_PREFIX_STYLE)));
                meterstandRepository.deleteInBatch(meterstandenInOneHour);
            }
        }
    }

    public Meterstand getMostRecent() {
        return mostRecentlySavedMeterstand;
    }

    public Meterstand getOldest() {
        return meterstandRepository.getOudste();
    }

    public Meterstand getOldestOfToday() {
        LocalDate today = LocalDate.now(clock);

        long van = toMillisSinceEpochAtStartOfDay(today);
        long totEnMet = toMillisSinceEpochAtStartOfDay(today.plusDays(1)) - 1;

        Meterstand oudsteStroomStandOpDag = meterstandRepository.getOudsteInPeriode(van, totEnMet);

        if (oudsteStroomStandOpDag != null) {
            // Gas is registered once every hour, in the hour AFTER it actually is used.
            // Compensate for that hour

            Meterstand oudsteGasStandOpDag = meterstandRepository.getOudsteInPeriode(van + MILLIS_PER_HOUR, totEnMet + MILLIS_PER_HOUR);

            if (oudsteGasStandOpDag != null) {
                oudsteStroomStandOpDag.setGas(oudsteGasStandOpDag.getGas());
            }
        }
        return oudsteStroomStandOpDag;
    }

    public List<MeterstandOpDag> perDag(DateTimePeriod period) {
        return getDaysInPeriod(period).stream()
                .map(this::getMeterstandOpDag)
                .collect(toList());
    }

    private MeterstandOpDag getMeterstandOpDag(LocalDate day) {
        return new MeterstandOpDag(day, getMeesteRecenteMeterstandOpDag(day));
    }

    private Meterstand getMeesteRecenteMeterstandOpDag(LocalDate day) {
        LocalDate today = LocalDate.now(clock);
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
        long van = toMillisSinceEpochAtStartOfDay(day);
        long totEnMet = toMillisSinceEpochAtStartOfDay(day.plusDays(1)) - 1;
        return meterstandRepository.getMeestRecenteInPeriode(van, totEnMet);
    }
}
