package nl.wiegman.home.energie;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DateTimeUtil;
import nl.wiegman.home.UpdateEvent;

@Service
public class MeterstandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterstandService.class);

    private final MeterstandRepository meterstandRepository;
    private final MeterstandServiceCached meterstandServiceCached;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public MeterstandService(MeterstandRepository meterstandRepository, MeterstandServiceCached meterstandServiceCached,
            ApplicationEventPublisher eventPublisher) {

        this.meterstandRepository = meterstandRepository;
        this.meterstandServiceCached = meterstandServiceCached;
        this.eventPublisher = eventPublisher;
    }

    public Meterstand save(Meterstand meterstand) {
        Meterstand savedMeterstand = meterstandRepository.save(meterstand);
        eventPublisher.publishEvent(new UpdateEvent(savedMeterstand));
        return savedMeterstand;
    }

    public Meterstand getMeestRecente() {
        LOGGER.info("getMeestRecente()");
        return meterstandRepository.getMeestRecente();
    }

    public Meterstand getOudste() {
        LOGGER.info("getOudste()");
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
