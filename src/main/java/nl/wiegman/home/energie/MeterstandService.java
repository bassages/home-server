package nl.wiegman.home.energie;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DateTimeUtil;
import nl.wiegman.home.cache.CacheService;

@Service
public class MeterstandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterstandService.class);

    private static final String TWO_AM = "0 0 2 * * *";

    private final MeterstandRepository meterstandRepository;
    private final MeterstandServiceCached meterstandServiceCached;
    private final CacheService cacheService;

    @Autowired
    public MeterstandService(MeterstandRepository meterstandRepository, MeterstandServiceCached meterstandServiceCached,
            CacheService cacheService) {

        this.meterstandRepository = meterstandRepository;
        this.meterstandServiceCached = meterstandServiceCached;
        this.cacheService = cacheService;
    }

    public Meterstand save(Meterstand meterstand) {
        return meterstandRepository.save(meterstand);
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
                .collect(Collectors.groupingBy(item -> item.getDatumtijdAsDate().getHours()));

        byHour.values().forEach(this::cleanupMeterStandenInOneHour);

        cacheService.clear(VerbruikServiceCached.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
        cacheService.clear(VerbruikServiceCached.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
    }

    private void cleanupMeterStandenInOneHour(List<Meterstand> meterstandenInOneHour) {
        meterstandenInOneHour.sort(Comparator.comparing(Meterstand::getDatumtijd));

        if (meterstandenInOneHour.size() >= 2) {

            Meterstand firstInHour = meterstandenInOneHour.get(0);
            LOGGER.info("Keep first: " + firstInHour.getDatumtijdAsDate() + " - " + ReflectionToStringBuilder.toString(
                    firstInHour, ToStringStyle.SHORT_PREFIX_STYLE));
            meterstandenInOneHour.remove(firstInHour);

            Meterstand lastInHour = meterstandenInOneHour.get(meterstandenInOneHour.size() - 1);
            LOGGER.info("Keep last: " + lastInHour.getDatumtijdAsDate() + " - " + ReflectionToStringBuilder.toString(
                    lastInHour, ToStringStyle.SHORT_PREFIX_STYLE));
            meterstandenInOneHour.remove(lastInHour);

            if (isNotEmpty(meterstandenInOneHour)) {
                meterstandenInOneHour.forEach(meterstand -> LOGGER.info("Delete: " + ReflectionToStringBuilder.toString(meterstand, ToStringStyle.SHORT_PREFIX_STYLE)));
                meterstandRepository.deleteInBatch(meterstandenInOneHour);
            }
        }
    }

    public Meterstand getMeestRecente() {
        return meterstandRepository.getMeestRecente();
    }

    public Meterstand getOudste() {
        return meterstandRepository.getOudste();
    }

    public List<MeterstandOpDag> perDag(long van, long totEnMet) {
        List<MeterstandOpDag> result = new ArrayList<>();

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van, totEnMet);
        dagenInPeriode.forEach(dag -> {
            LOGGER.info("Ophalen laatste meterstand op dag: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dag));

            Meterstand meterstandOpDag = getMeesteRecenteMeterstandOpDag(dag);
            result.add(new MeterstandOpDag(dag.getTime(), meterstandOpDag));
        });
        return result;
    }

    public Meterstand getOudsteMeterstandOpDag(Date dag) {
        if (DateTimeUtil.isAfterToday(dag)) {
            return null;
        } else {
            return meterstandServiceCached.getOudsteMeterstandOpDag(dag);
        }
    }

    private Meterstand getMeesteRecenteMeterstandOpDag(Date dag) {
        if (DateTimeUtil.isAfterToday(dag)) {
            return null;
        } else if (DateUtils.isSameDay(new Date(), dag)) {
            return meterstandServiceCached.getMeestRecenteMeterstandOpDag(dag);
        } else {
            return meterstandServiceCached.getPotentiallyCachedMeestRecenteMeterstandOpDag(dag);
        }
    }
}
