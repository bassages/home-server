package nl.wiegman.home.service;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.realtime.UpdateEvent;
import nl.wiegman.home.repository.BinnenklimaatRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

/**
 * Receives updates from klimaat sensors, stores the values and provides them.
 */
@Component
public class KlimaatService {

    private static final Logger LOG = LoggerFactory.getLogger(KlimaatService.class);

    private final List<Klimaat> receivedInLastQuarter = new ArrayList<>();

    @Inject
    private BinnenklimaatRepository binnenklimaatRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0/15 * * * ?") // Every 15 minutes
    public void save() {
        LOG.debug("Running " + this.getClass().getSimpleName() + ".save()");

        Date now = new Date();

        List<BigDecimal> validTemperaturesFromLastQuarter = getValidTemperaturesFromLastQuarter();

        receivedInLastQuarter.clear();

        if (validTemperaturesFromLastQuarter.size() >= 2) {
            BigDecimal totalTemperature = BigDecimal.ZERO;

            for (BigDecimal validTemperatuur : validTemperaturesFromLastQuarter) {
                totalTemperature.add(validTemperatuur);
            }
            BigDecimal averageTemperatuur = totalTemperature.divide(BigDecimal.valueOf(validTemperaturesFromLastQuarter.size()));

            DateUtils.setMilliseconds(now, 0);
            DateUtils.setSeconds(now, 0);

            Klimaat klimaatToSave = new Klimaat();
            klimaatToSave.setDatumtijd(now.getTime());
            klimaatToSave.setTemperatuur(averageTemperatuur);

            Klimaat savedKlimaat = binnenklimaatRepository.save(klimaatToSave);

            eventPublisher.publishEvent(new UpdateEvent(savedKlimaat));
        }
    }

    public void add(Klimaat klimaat) {
        LOG.info("Recieved klimaat");
        receivedInLastQuarter.add(klimaat);
    }

    private List<BigDecimal> getValidTemperaturesFromLastQuarter() {
        List<BigDecimal> result = new ArrayList<>();

        for (Klimaat klimaat : receivedInLastQuarter) {
            if (!BigDecimal.ZERO.equals(klimaat.getTemperatuur())) {
                result.add(klimaat.getTemperatuur());
            }
        }
        return result;
    }
}