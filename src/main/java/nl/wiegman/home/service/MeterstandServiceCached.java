package nl.wiegman.home.service;

import nl.wiegman.home.model.Meterstand;
import nl.wiegman.home.repository.MeterstandRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;

@Service
public class MeterstandServiceCached {

    @Inject
    private MeterstandRepository meterstandRepository;

    @Cacheable(cacheNames = "meestRecenteMeterstandOpDag")
    public Meterstand getPotentiallyCachedMeestRecenteMeterstandOpDag(Date dag) {
        return getMeestRecenteMeterstandOpDag(dag);
    }

    public Meterstand getMeestRecenteMeterstandOpDag(Date dag) {
        return meterstandRepository.getMeestRecenteInPeriode(DateTimeUtil.getStartOfDay(dag), DateTimeUtil.getEndOfDay(dag));
    }

    public Meterstand getOudsteMeterstandOpDag(Date dag) {
        return meterstandRepository.getOudsteInPeriode(DateTimeUtil.getStartOfDay(dag), DateTimeUtil.getEndOfDay(dag));
    }

}
