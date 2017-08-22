package nl.wiegman.home.energie;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class OpgenomenVermogenService {

    private final OpgenomenVermogenRepository opgenomenVermogenRepository;

    @Autowired
    public OpgenomenVermogenService(OpgenomenVermogenRepository opgenomenVermogenRepository) {
        this.opgenomenVermogenRepository = opgenomenVermogenRepository;
    }

    public OpgenomenVermogen save(OpgenomenVermogen opgenomenVermogen) {
        return opgenomenVermogenRepository.save(opgenomenVermogen);
    }

    public OpgenomenVermogen getMeestRecente() {
        return opgenomenVermogenRepository.getMeestRecente();
    }

    @Cacheable(cacheNames = "opgenomenVermogenHistory")
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

    private static class Period {
        private long from, to;
        private Period(long from , long to) {
            this.from = from;
            this.to = to;
        }
    }
}
