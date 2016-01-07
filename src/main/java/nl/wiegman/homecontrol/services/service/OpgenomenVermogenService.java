package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpgenomenVermogenService {

    @Inject
    MeterstandRepository meterstandRepository;

    @Cacheable(cacheNames = "opgenomenVermogenHistory")
    public List<OpgenomenVermogen> getPotentiallyCachedOpgenomenVermogenHistory(long from, long to, long subPeriodLength) {
        return getOpgenomenVermogenHistory(from, to, subPeriodLength);
    }

    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(long from, long to, long subPeriodLength) {
        List<OpgenomenVermogen> result = new ArrayList<>();

        List<Meterstand> list = meterstandRepository.getMeterstanden(from, to);

        long nrOfSubPeriodsInPeriod = (to-from)/subPeriodLength;

        for (int i=0; i<=nrOfSubPeriodsInPeriod; i++) {
            long subStart = from + (i * subPeriodLength);
            long subEnd = subStart + subPeriodLength;

            OpgenomenVermogen vermogenInPeriode = getMaximumOpgenomenVermogenInPeriode(list, subStart, subEnd);
            if (vermogenInPeriode != null) {
                vermogenInPeriode.setDatumtijd(subStart);
                result.add(vermogenInPeriode);
            } else {
                result.add(new OpgenomenVermogen(subStart, 0));
            }
        }
        return result;
    }

    private OpgenomenVermogen getMaximumOpgenomenVermogenInPeriode(List<Meterstand> list, long start, long end) {
        return list.stream()
                .filter(ov -> ov.getDatumtijd() >= start && ov.getDatumtijd() < end)
                .map(m -> new OpgenomenVermogen(m.getDatumtijd(), m.getStroomOpgenomenVermogenInWatt()))
                .max((ov1, ov2) -> Integer.compare(ov1.getOpgenomenVermogenInWatt(), ov2.getOpgenomenVermogenInWatt()))
                .orElse(null);
    }
}
