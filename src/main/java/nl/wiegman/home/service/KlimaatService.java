package nl.wiegman.home.service;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.realtime.UpdateEvent;
import nl.wiegman.home.repository.KlimaatRepo;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Receives updates from klimaat sensors, stores the values and provides them.
 */
@Component
public class KlimaatService {

    private static final Logger LOG = LoggerFactory.getLogger(KlimaatService.class);

    private final List<Klimaat> receivedInLastQuarter = new ArrayList<>();

    @Inject
    private KlimaatServiceCached klimaatServiceCached;

    @Inject
    private KlimaatRepo klimaatRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0/15 * * * ?") // Every 15 minutes
    public void save() {
        LOG.debug("Running " + this.getClass().getSimpleName() + ".save()");

        Date now = new Date();
        now = DateUtils.setMilliseconds(now, 0);
        now = DateUtils.setSeconds(now, 0);

        List<BigDecimal> validTemperaturesFromLastQuarter = getValidTemperaturesFromLastQuarter();
        List<BigDecimal> validHumiditiesFromLastQuarter = getValidHumiditiesFromLastQuarter();

        receivedInLastQuarter.clear();

        BigDecimal averageTemperature = getAverage(validTemperaturesFromLastQuarter);
        if (averageTemperature != null) {
            averageTemperature = averageTemperature.setScale(2, RoundingMode.CEILING);
        }

        BigDecimal averageHumidity = getAverage(validHumiditiesFromLastQuarter);
        if (averageHumidity != null) {
            averageHumidity = averageHumidity.setScale(1, RoundingMode.CEILING);
        }

        if (averageTemperature != null || averageHumidity != null) {
            Klimaat klimaatToSave = new Klimaat();
            klimaatToSave.setDatumtijd(now);
            klimaatToSave.setTemperatuur(averageTemperature);
            klimaatToSave.setLuchtvochtigheid(averageHumidity);
            klimaatRepository.save(klimaatToSave);
        }
    }

    private BigDecimal getAverage(List<BigDecimal> decimals) {
        if (decimals.isEmpty()) {
            return null;
        } else {
            BigDecimal total = BigDecimal.ZERO;

            for (BigDecimal decimal : decimals) {
                total = total.add(decimal);
            }
            return total.divide(BigDecimal.valueOf(decimals.size()), RoundingMode.CEILING);
        }
    }

    public List<Klimaat> getInPeriod(Date from, Date to) {
        if (to.before(new Date())) {
            return klimaatServiceCached.getInPeriod(from, to);
        } else {
            return klimaatRepository.findByDatumtijdBetweenOrderByDatumtijd(from, to);
        }
    }

    public Klimaat getMostRecent() {
        LOG.info("getMostRecent()");
        return klimaatRepository.getMostRecent();
    }

    public void add(Klimaat klimaat) {
        LOG.info("Recieved klimaat");
        receivedInLastQuarter.add(klimaat);
        eventPublisher.publishEvent(new UpdateEvent(klimaat));
    }

    private List<BigDecimal> getValidTemperaturesFromLastQuarter() {
        List<BigDecimal> result = new ArrayList<>();

        for (Klimaat klimaat : receivedInLastQuarter) {
            if (klimaat.getTemperatuur() != null && !BigDecimal.ZERO.equals(klimaat.getTemperatuur())) {
                result.add(klimaat.getTemperatuur());
            }
        }
        return result;
    }

    public List<BigDecimal> getValidHumiditiesFromLastQuarter() {
        List<BigDecimal> result = new ArrayList<>();

        for (Klimaat klimaat : receivedInLastQuarter) {
            if (klimaat.getLuchtvochtigheid() != null && klimaat.getLuchtvochtigheid().doubleValue() > 0.0) {
                result.add(klimaat.getLuchtvochtigheid());
            }
        }
        return result;
    }

    public List<Klimaat> getHighest(String sensortype, Date from, Date to, int limit) {
        List<Date> peakTemperatureDates = klimaatRepository.getPeakHighTemperatureDates(from, to, limit);

        List<Klimaat> result = new ArrayList<>();

        for (Date date : peakTemperatureDates) {
            result.add(klimaatRepository.firstHighestTemperatureOnDay(date));
        }
        return result;
    }

    public List<Klimaat> getLowest(String sensortype, Date from, Date to, int limit) {
        List<Date> peakTemperatureDates = klimaatRepository.getPeakLowTemperatureDates(from, to, limit);

        List<Klimaat> result = new ArrayList<>();

        for (Date date : peakTemperatureDates) {
            result.add(klimaatRepository.firstLowestTemperatureOnDay(date));
        }
        return result;
    }
}
