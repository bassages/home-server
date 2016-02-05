package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.repository.MeterstandRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

@Component
public class MeterstandServiceCached {

    @Inject
    private MeterstandRepository meterstandRepository;

    @Cacheable(cacheNames = "meterstandOpDag")
    public Meterstand getPotentiallyCachedMeterstandOpDag(Date dag) {
        return getMeterstandOpDag(dag);
    }

    public Meterstand getMeterstandOpDag(Date dag) {
        return meterstandRepository.getMeestRecenteInPeriode(DateTimeUtil.getStartOfDay(dag), DateTimeUtil.getEndOfDay(dag));
    }
}
