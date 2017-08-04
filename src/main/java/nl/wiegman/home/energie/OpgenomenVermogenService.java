package nl.wiegman.home.energie;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class OpgenomenVermogenService {

    private final MeterstandRepository meterstandRepository;

    @Autowired
    public OpgenomenVermogenService(MeterstandRepository meterstandRepository) {
        this.meterstandRepository = meterstandRepository;
    }

    @Cacheable(cacheNames = "opgenomenVermogenHistory")
    public List<OpgenomenVermogen> getPotentiallyCachedOpgenomenStroomVermogenHistory(long from, long to, long subPeriodLength) {
        return getOpgenomenStroomVermogenHistory(from, to, subPeriodLength);
    }

    public List<OpgenomenVermogen> getOpgenomenStroomVermogenHistory(long from, long to, long subPeriodLength) {
        List<Meterstand> meterstandenInPeriod = meterstandRepository.getMeterstanden(from, to);

        long nrOfSubPeriodsInPeriod = (to-from)/subPeriodLength;

        return LongStream.rangeClosed(0, nrOfSubPeriodsInPeriod).boxed()
                .map(periodNumber -> this.toSubPeriod(periodNumber, from, subPeriodLength))
                .map(subPeriod -> this.getMaxOpgenomenVermogenInPeriode(meterstandenInPeriod, subPeriod))
                .collect(Collectors.toList());
    }

    private Period toSubPeriod(long periodNumber, long start, long subPeriodLength) {
        long subStart = start + (periodNumber * subPeriodLength);
        long subEnd = subStart + subPeriodLength;
        return new Period(subStart, subEnd);
    }

    private OpgenomenVermogen getMaxOpgenomenVermogenInPeriode(List<Meterstand> meterstanden, Period period) {
        return meterstanden.stream()
                .filter(meterstand -> meterstand.getDatumtijd() >= period.from && meterstand.getDatumtijd() < period.to)
                .max(Comparator.comparingInt(Meterstand::getStroomOpgenomenVermogenInWatt))
                .map(meterstand1 -> new OpgenomenVermogen(period.from, meterstand1.getStroomOpgenomenVermogenInWatt(), meterstand1.getStroomTariefIndicator()))
                .orElse(new OpgenomenVermogen(period.from, 0, null));
    }

    private static class Period {
        private long from, to;
        private Period(long from , long to) {
            this.from = from;
            this.to = to;
        }
    }
}
