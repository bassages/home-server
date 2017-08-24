package nl.wiegman.home.energie;

import static org.apache.commons.collections.CollectionUtils.*;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DateTimeUtil;
import nl.wiegman.home.cache.CacheService;

@Service
public class OpgenomenVermogenService {

    private static final Logger LOG = LoggerFactory.getLogger(OpgenomenVermogenService.class);

    private static final String CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY = "opgenomenVermogenHistory";
    private static final String ONE_AM = "0 0 1 * * *";

    private final CacheService cacheService;
    private final OpgenomenVermogenRepository opgenomenVermogenRepository;

    @Autowired
    public OpgenomenVermogenService(CacheService cacheService, OpgenomenVermogenRepository opgenomenVermogenRepository) {
        this.cacheService = cacheService;
        this.opgenomenVermogenRepository = opgenomenVermogenRepository;
    }

    @Scheduled(cron = ONE_AM)
    public void dailyCleanup() {
        Date today = new Date();
        cleanup(DateUtils.addDays(today, -1));
        cleanup(DateUtils.addDays(today, -2));
        cleanup(DateUtils.addDays(today, -3));
    }

    public OpgenomenVermogen save(OpgenomenVermogen opgenomenVermogen) {
        return opgenomenVermogenRepository.save(opgenomenVermogen);
    }

    public OpgenomenVermogen getMeestRecente() {
        return opgenomenVermogenRepository.getMeestRecente();
    }

    @Cacheable(cacheNames = CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY)
    public List<OpgenomenVermogen> getPotentiallyCachedHistory(Date from, Date to, long subPeriodLength) {
        return getHistory(from, to, subPeriodLength);
    }

    public List<OpgenomenVermogen> getHistory(Date from, Date to, long subPeriodLength) {
        List<OpgenomenVermogen> opgenomenVermogenInPeriod = opgenomenVermogenRepository.getOpgenomenVermogen(from, to);

        long nrOfSubPeriodsInPeriod = (to.getTime()-from.getTime())/subPeriodLength;

        return LongStream.rangeClosed(0, nrOfSubPeriodsInPeriod).boxed()
                .map(periodNumber -> this.toSubPeriod(periodNumber, from.getTime(), subPeriodLength))
                .map(subPeriod -> this.getMaxOpgenomenVermogenInPeriode(opgenomenVermogenInPeriod, subPeriod))
                .collect(Collectors.toList());
    }

    private Period toSubPeriod(long periodNumber, long start, long subPeriodLength) {
        long subStart = start + (periodNumber * subPeriodLength);
        long subEnd = subStart + subPeriodLength;
        return new Period(subStart, subEnd);
    }

    private OpgenomenVermogen getMaxOpgenomenVermogenInPeriode(List<OpgenomenVermogen> opgenomenVermogens, Period period) {
        return opgenomenVermogens.stream()
                .filter(opgenomenVermogen -> opgenomenVermogen.getDatumtijd().getTime() >= period.from && opgenomenVermogen.getDatumtijd().getTime() < period.to)
                .max(Comparator.comparingInt(OpgenomenVermogen::getWatt))
                .map(o -> this.mapToOpgenomenVermogen(o, period))
                .orElse(this.mapToEmptyOpgenomenVermogen(period.from));
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(OpgenomenVermogen opgenomenVermogen, Period period) {
        OpgenomenVermogen result = new OpgenomenVermogen();
        result.setTariefIndicator(opgenomenVermogen.getTariefIndicator());
        result.setDatumtijd(new Date(period.from));
        result.setWatt(opgenomenVermogen.getWatt());
        return result;
    }

    private OpgenomenVermogen mapToEmptyOpgenomenVermogen(long datumtijd) {
        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(new Date(datumtijd));
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.ONBEKEND);
        opgenomenVermogen.setWatt(0);
        return opgenomenVermogen;
    }

    public void cleanup(Date date) {
        Date from = DateTimeUtil.getStartOfDayAsDate(date);
        Date to = DateUtils.addDays(from, 1);

        List<OpgenomenVermogen> opgenomenVermogensOnDay = opgenomenVermogenRepository.getOpgenomenVermogen(from, to);

        Map<Integer, List<OpgenomenVermogen>> byHour = opgenomenVermogensOnDay.stream()
                .collect(Collectors.groupingBy(item -> item.getDatumtijd().getHours()));

        byHour.values().forEach(this::cleanupHour);

        cacheService.clear(CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY);
    }

    private void cleanupHour(List<OpgenomenVermogen> opgenomenVermogensInOneHour) {
        Map<Integer, List<OpgenomenVermogen>> byMinute = opgenomenVermogensInOneHour.stream()
                .collect(Collectors.groupingBy(item -> item.getDatumtijd().getMinutes()));

        List<OpgenomenVermogen> opgenomenVermogensToKeep = byMinute.values().stream().map(this::getOpgenomenVermogenToKeepInMinute).collect(Collectors.toList());

        opgenomenVermogensInOneHour.removeAll(opgenomenVermogensToKeep);

        opgenomenVermogensToKeep.forEach(opgenomenVermogen -> LOG.info("Keep: " + ReflectionToStringBuilder.toString(opgenomenVermogen, ToStringStyle.SHORT_PREFIX_STYLE    )));
        opgenomenVermogensInOneHour.forEach(opgenomenVermogen -> LOG.info("Delete: " + ReflectionToStringBuilder.toString(opgenomenVermogen, ToStringStyle.SHORT_PREFIX_STYLE)));

        if (isNotEmpty(opgenomenVermogensInOneHour)) {
            opgenomenVermogenRepository.deleteInBatch(opgenomenVermogensInOneHour);
        }
    }

    private OpgenomenVermogen getOpgenomenVermogenToKeepInMinute(List<OpgenomenVermogen> opgenomenVermogenInOneMinute) {
        return opgenomenVermogenInOneMinute.stream()
                .max(Comparator.comparingInt(OpgenomenVermogen::getWatt).thenComparing(Comparator.comparing(OpgenomenVermogen::getDatumtijd))).get();
    }

    private static class Period {
        private long from, to;
        private Period(long from, long to) {
            this.from = from;
            this.to = to;
        }
    }
}
