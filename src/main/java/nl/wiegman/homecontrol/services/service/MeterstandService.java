package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.model.api.MeterstandOpDag;
import nl.wiegman.homecontrol.services.model.event.UpdateEvent;
import nl.wiegman.homecontrol.services.repository.MeterstandRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class MeterstandService {

    private final Logger logger = LoggerFactory.getLogger(MeterstandService.class);

    @Inject
    private MeterstandRepository meterstandRepository;

    @Inject
    private MeterstandServiceCached meterstandServiceCached;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void opslaanMeterstand(Meterstand meterstand) {
        logger.info("Save for " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(meterstand.getDatumtijd())));

        meterstand.setGas(meterstand.getGas().setScale(3, RoundingMode.CEILING));
        meterstand.setStroomTarief1(meterstand.getStroomTarief1().setScale(3, RoundingMode.CEILING));
        meterstand.setStroomTarief2(meterstand.getStroomTarief2().setScale(3, RoundingMode.CEILING));

        meterstandRepository.save(meterstand);

        eventPublisher.publishEvent(new UpdateEvent(meterstand));
    }

    public Meterstand getMeestRecente() {
        logger.info("getMeestRecente()");
        return meterstandRepository.getMeestRecente();
    }

    public Meterstand getOudste() {
        logger.info("getOudste()");
        return meterstandRepository.getOudste();
    }

    public List<MeterstandOpDag> perDag(long van, long totEnMet) {
        List<MeterstandOpDag> result = new ArrayList<>();

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van, totEnMet);
        dagenInPeriode.forEach(dag -> {
            logger.info("Ophalen laatste meterstand op dag: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dag));

            Meterstand meterstandOpDag = getMeterstandOpDag(dag);
            result.add(new MeterstandOpDag(dag.getTime(), meterstandOpDag));
        });
        return result;
    }

    private Meterstand getMeterstandOpDag(Date dag) {
        if (DateTimeUtil.isAfterToday(dag)) {
            return null;
        } else if (DateUtils.isSameDay(new Date(), dag)) {
            return meterstandServiceCached.getMeterstandOpDag(dag);
        } else {
            return meterstandServiceCached.getPotentiallyCachedMeterstandOpDag(dag);
        }
    }
}
