package nl.wiegman.home.service;

import nl.wiegman.home.model.Meterstand;
import nl.wiegman.home.repository.MeterstandRepository;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MeterstandServiceCached {

    @Autowired
    private MeterstandRepository meterstandRepository;

    @Cacheable(cacheNames = "meestRecenteMeterstandOpDag")
    public Meterstand getPotentiallyCachedMeestRecenteMeterstandOpDag(Date dag) {
        return getMeestRecenteMeterstandOpDag(dag);
    }

    public Meterstand getMeestRecenteMeterstandOpDag(Date dag) {
        return meterstandRepository.getMeestRecenteInPeriode(DateTimeUtil.getStartOfDay(dag), DateTimeUtil.getEndOfDay(dag));
    }

    public Meterstand getOudsteMeterstandOpDag(Date dag) {
        Meterstand oudsteStroomStandOpDag = meterstandRepository.getOudsteInPeriode(DateTimeUtil.getStartOfDay(dag), DateTimeUtil.getEndOfDay(dag));

        // Gas is registered once every hour, in the hour AFTER it actually is used.
        // Compensate for that hour
        Meterstand oudsteGasStandOpDag = meterstandRepository.getOudsteInPeriode(DateTimeUtil.getStartOfDay(dag) + DateUtils.MILLIS_PER_HOUR, DateTimeUtil.getEndOfDay(dag) + DateUtils.MILLIS_PER_HOUR);
        oudsteStroomStandOpDag.setGas(oudsteGasStandOpDag.getGas());

        return oudsteStroomStandOpDag;
    }

}
