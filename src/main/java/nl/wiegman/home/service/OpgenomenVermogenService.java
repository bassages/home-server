package nl.wiegman.home.service;

import nl.wiegman.home.model.Meterstand;
import nl.wiegman.home.api.dto.OpgenomenVermogen;
import nl.wiegman.home.repository.MeterstandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        List<OpgenomenVermogen> result = new ArrayList<>();

        List<Meterstand> list = meterstandRepository.getMeterstanden(from, to);

        long nrOfSubPeriodsInPeriod = (to-from)/subPeriodLength;

        for (int i = 0; i <= nrOfSubPeriodsInPeriod; i++) {
            long subStart = from + (i * subPeriodLength);
            long subEnd = subStart + subPeriodLength;

            OpgenomenVermogen vermogenInPeriode = getMaximumOpgenomenVermogenInPeriode(list, subStart, subEnd);
            if (vermogenInPeriode != null) {
                vermogenInPeriode.setDatumtijd(subStart);
                result.add(vermogenInPeriode);
            } else {
                result.add(new OpgenomenVermogen(subStart, 0, null));
            }
        }
        return result;
    }

    private OpgenomenVermogen getMaximumOpgenomenVermogenInPeriode(List<Meterstand> list, long start, long end) {
        return list.stream()
                .filter(ov -> ov.getDatumtijd() >= start && ov.getDatumtijd() < end)
                .map(m -> new OpgenomenVermogen(m.getDatumtijd(), m.getStroomOpgenomenVermogenInWatt(), m.getStroomTariefIndicator()))
                .max(Comparator.comparingInt(OpgenomenVermogen::getOpgenomenVermogenInWatt))
                .orElse(null);
    }
}
