package nl.wiegman.home.service;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.model.SensorType;
import nl.wiegman.home.realtime.UpdateEvent;
import nl.wiegman.home.repository.KlimaatRepos;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class KlimaatService {
    private static final Logger LOG = LoggerFactory.getLogger(KlimaatService.class);

    private static final String EVERY_15_MINUTES_PAST_THE_HOUR = "0 0/15 * * * ?";

    private final List<Klimaat> receivedInLastQuarter = new ArrayList<>();

    @Autowired
    KlimaatServiceCached klimaatServiceCached;

    @Autowired
    KlimaatRepos klimaatRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = EVERY_15_MINUTES_PAST_THE_HOUR)
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
        BigDecimal average = null;
        if (!decimals.isEmpty()) {
            BigDecimal total = decimals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            average = total.divide(BigDecimal.valueOf(decimals.size()), RoundingMode.CEILING);
        }
        return average;
    }

    public List<Klimaat> getInPeriod(Date from, Date to) {
        if (to.before(new Date())) {
            return klimaatServiceCached.getInPeriod(from, to);
        } else {
            return klimaatRepository.findByDatumtijdBetweenOrderByDatumtijd(from, to);
        }
    }

    public BigDecimal getAverage(SensorType sensortype, Date from, Date to) {
        switch (sensortype) {
            case TEMPERATUUR:
                return klimaatRepository.getAverageTemperatuur(from, to);
            case LUCHTVOCHTIGHEID:
                return klimaatRepository.getAverageLuchtvochtigheid(from, to);
            default:
                return null;
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

        receivedInLastQuarter.stream()
                .filter(klimaat -> klimaat.getTemperatuur() != null && !BigDecimal.ZERO.equals(klimaat.getTemperatuur()))
                .forEach(klimaat -> result.add(klimaat.getTemperatuur()));

        return result;
    }

    public List<BigDecimal> getValidHumiditiesFromLastQuarter() {
        List<BigDecimal> result = new ArrayList<>();

        receivedInLastQuarter.stream()
                .filter(klimaat -> klimaat.getLuchtvochtigheid() != null && !BigDecimal.ZERO.equals(klimaat.getLuchtvochtigheid()))
                .forEach(klimaat -> result.add(klimaat.getLuchtvochtigheid()));

        return result;
    }

    public List<Klimaat> getHighest(SensorType sensortype, Date from, Date to, int limit) {
        switch (sensortype) {
            case TEMPERATUUR:
                return getHighestTemperature(from, to, limit);
            case LUCHTVOCHTIGHEID:
                return getHighestHumidity(from, to, limit);
            default:
                return Collections.emptyList();
        }
    }

    private List<Klimaat> getHighestTemperature(Date from, Date to, int limit) {
        List<Klimaat> result = new ArrayList<>();

        klimaatRepository.getPeakHighTemperatureDates(from, to, limit)
            .forEach(date -> result.add(klimaatRepository.firstHighestTemperatureOnDay(date)));

        return result;
    }

    private List<Klimaat> getHighestHumidity(Date from, Date to, int limit) {
        List<Klimaat> result = new ArrayList<>();

        klimaatRepository.getPeakHighHumidityDates(from, to, limit)
                .forEach(date -> result.add(klimaatRepository.firstHighestHumidityOnDay(date)));

        return result;
    }

    public List<Klimaat> getLowest(SensorType sensortype, Date from, Date to, int limit) {
        switch (sensortype) {
            case TEMPERATUUR:
                return getLowestTemperature(from, to, limit);
            case LUCHTVOCHTIGHEID:
                return getLowestHumidity(from, to, limit);
            default:
                return null;
        }
    }

    private List<Klimaat> getLowestTemperature(Date from, Date to, int limit) {
        List<Date> peakTemperatureDates = klimaatRepository.getPeakLowTemperatureDates(from, to, limit);

        List<Klimaat> result = new ArrayList<>();

        for (Date date : peakTemperatureDates) {
            result.add(klimaatRepository.firstLowestTemperatureOnDay(date));
        }
        return result;
    }

    private List<Klimaat> getLowestHumidity(Date from, Date to, int limit) {
        List<Date> peakHumidityDates = klimaatRepository.getPeakLowHumidityDates(from, to, limit);

        List<Klimaat> result = new ArrayList<>();

        for (Date date : peakHumidityDates) {
            result.add(klimaatRepository.firstLowestHumidityOnDay(date));
        }
        return result;
    }
}
