package nl.wiegman.home.energie;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static nl.wiegman.home.DateTimeUtil.toDateAtStartOfDay;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DateTimePeriod;
import nl.wiegman.home.DateTimeUtil;
import nl.wiegman.home.cache.CacheService;

@Service
public class MeterstandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterstandService.class);

    private static final String TWO_AM = "0 0 2 * * *";

    private final MeterstandRepository meterstandRepository;
    private final MeterstandServiceCached meterstandServiceCached;
    private final CacheService cacheService;
    private final Clock clock;

    private Meterstand mostRecentlySavedMeterstand = null;

    @Autowired
    public MeterstandService(MeterstandRepository meterstandRepository, MeterstandServiceCached meterstandServiceCached, CacheService cacheService, Clock clock) {
        this.meterstandRepository = meterstandRepository;
        this.meterstandServiceCached = meterstandServiceCached;
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
        Date today = new Date();
        cleanup(DateUtils.addDays(today, -1));
        cleanup(DateUtils.addDays(today, -2));
        cleanup(DateUtils.addDays(today, -3));
    }

    public void cleanup(Date date) {
        Date from = DateTimeUtil.getStartOfDayAsDate(date);
        Date to = DateUtils.addDays(from, 1);

        List<Meterstand> meterstandenOnDay = meterstandRepository.findByDatumtijdBetween(from.getTime(), to.getTime()-1);

        Map<Integer, List<Meterstand>> byHour = meterstandenOnDay.stream()
                .collect(groupingBy(item -> item.getDatumtijdAsLocalDateTime().getHour()));

        byHour.values().forEach(this::cleanupMeterStandenInOneHour);

        cacheService.clear(VerbruikServiceCached.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
        cacheService.clear(VerbruikServiceCached.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
    }

    private void cleanupMeterStandenInOneHour(List<Meterstand> meterstandenInOneHour) {
        meterstandenInOneHour.sort(comparing(Meterstand::getDatumtijd));

        if (meterstandenInOneHour.size() >= 2) {

            Meterstand firstInHour = meterstandenInOneHour.get(0);
            LOGGER.info("Keep first: " + firstInHour.getDatumtijdAsLocalDateTime() + " - " + ReflectionToStringBuilder.toString(
                    firstInHour, ToStringStyle.SHORT_PREFIX_STYLE));
            meterstandenInOneHour.remove(firstInHour);

            Meterstand lastInHour = meterstandenInOneHour.get(meterstandenInOneHour.size() - 1);
            LOGGER.info("Keep last: " + lastInHour.getDatumtijdAsLocalDateTime() + " - " + ReflectionToStringBuilder.toString(
                    lastInHour, ToStringStyle.SHORT_PREFIX_STYLE));
            meterstandenInOneHour.remove(lastInHour);

            if (isNotEmpty(meterstandenInOneHour)) {
                meterstandenInOneHour.forEach(meterstand -> LOGGER.info("Delete: " + ReflectionToStringBuilder.toString(meterstand, ToStringStyle.SHORT_PREFIX_STYLE)));
                meterstandRepository.deleteInBatch(meterstandenInOneHour);
            }
        }
    }

    public Meterstand getMeestRecente() {
        return mostRecentlySavedMeterstand;
    }

    public Meterstand getOudste() {
        return meterstandRepository.getOudste();
    }

    public List<MeterstandOpDag> perDag(DateTimePeriod period) {
        List<MeterstandOpDag> result = new ArrayList<>();

        List<LocalDate> dagenInPeriode = DateTimeUtil.getDagenInPeriode(period);
        dagenInPeriode.forEach(dag -> {
            Meterstand meterstandOpDag = getMeesteRecenteMeterstandOpDag(dag);
            result.add(new MeterstandOpDag(toDateAtStartOfDay(dag).getTime(), meterstandOpDag));
        });
        return result;
    }

    public Meterstand getOudsteMeterstandOpDag(LocalDate day) {
        if (day.isAfter(LocalDate.now(clock))) {
            return null;
        } else {
            return meterstandServiceCached.getOudsteMeterstandOpDag(DateTimeUtil.toDateAtStartOfDay(day));
        }
    }

    private Meterstand getMeesteRecenteMeterstandOpDag(LocalDate day) {
        LocalDate now = LocalDate.now(clock);
        if (day.isAfter(now)) {
            return null;
        } else if (day.isEqual(now)) {
            return meterstandServiceCached.getMeestRecenteMeterstandOpDag(toDateAtStartOfDay(day));
        } else {
            return meterstandServiceCached.getPotentiallyCachedMeestRecenteMeterstandOpDag(toDateAtStartOfDay(day));
        }
    }
}
