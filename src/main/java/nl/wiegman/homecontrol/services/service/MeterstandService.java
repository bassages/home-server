package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.model.api.MeterstandOpDag;
import nl.wiegman.homecontrol.services.model.event.UpdateEvent;
import nl.wiegman.homecontrol.services.repository.MeterstandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class MeterstandService {

    private final Logger logger = LoggerFactory.getLogger(MeterstandService.class);

    @Inject
    private MeterstandRepository meterstandRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void opslaanMeterstand(Meterstand meterstand) {
        logger.info("Save for " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(meterstand.getDatumtijd())));
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

            Meterstand meterstandOpDag = getMeterstandOpDag(DateTimeUtil.getStartOfDay(dag), DateTimeUtil.getEndOfDay(dag));
            result.add(new MeterstandOpDag(dag.getTime(), meterstandOpDag));
        });

        return result;
    }

    private Meterstand getMeterstandOpDag(long van, long totEnMet) {
        return meterstandRepository.getMeestRecenteInPeriode(van, totEnMet);
    }
}
