package nl.wiegman.home.klimaat;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.wiegman.home.UpdateEvent;

@Service
public class KlimaatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KlimaatService.class);

    private static final String EVERY_15_MINUTES_PAST_THE_HOUR = "0 0/15 * * * ?";

    private static final int TEMPERATURE_SCALE = 2;
    private static final int HUMIDITY_SCALE = 1;

    private final Map<String, List<Klimaat>> receivedInLastQuarter = new ConcurrentHashMap<>();

    private final KlimaatServiceCached klimaatServiceCached;
    private final KlimaatRepos klimaatRepository;
    private final KlimaatSensorRepository klimaatSensorRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public KlimaatService(KlimaatServiceCached klimaatServiceCached, KlimaatRepos klimaatRepository, KlimaatSensorRepository klimaatSensorRepository,
            ApplicationEventPublisher eventPublisher) {

        this.klimaatServiceCached = klimaatServiceCached;
        this.klimaatRepository = klimaatRepository;
        this.klimaatSensorRepository = klimaatSensorRepository;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void createSensor() {
        if (CollectionUtils.isEmpty(klimaatSensorRepository.findAll())) {
            KlimaatSensor klimaatSensor = new KlimaatSensor();
            klimaatSensor.setCode("WOONKAMER");
            klimaatSensor.setOmschrijving("Huiskamer");
            klimaatSensorRepository.save(klimaatSensor);
        }
    }

    @Scheduled(cron = EVERY_15_MINUTES_PAST_THE_HOUR)
    public void save() {
        LOGGER.debug("Running " + this.getClass().getSimpleName() + ".save()");

        Date now = new Date();
        now = DateUtils.truncate(now, Calendar.MINUTE);

        for (Map.Entry<String, List<Klimaat>> receivedItem : receivedInLastQuarter.entrySet()) {
            List<BigDecimal> validTemperaturesFromLastQuarter = getValidTemperatures(receivedItem.getValue());
            List<BigDecimal> validHumiditiesFromLastQuarter = getValidHumidities(receivedItem.getValue());

            KlimaatSensor klimaatSensor = receivedItem.getValue().get(0).getKlimaatSensor();

            BigDecimal averageTemperature = getAverage(validTemperaturesFromLastQuarter);
            if (averageTemperature != null) {
                averageTemperature = averageTemperature.setScale(TEMPERATURE_SCALE, RoundingMode.HALF_UP);
            }

            BigDecimal averageHumidity = getAverage(validHumiditiesFromLastQuarter);
            if (averageHumidity != null) {
                averageHumidity = averageHumidity.setScale(HUMIDITY_SCALE, RoundingMode.HALF_UP);
            }

            if (averageTemperature != null || averageHumidity != null) {
                Klimaat klimaatToSave = new Klimaat();
                klimaatToSave.setDatumtijd(now);
                klimaatToSave.setTemperatuur(averageTemperature);
                klimaatToSave.setLuchtvochtigheid(averageHumidity);
                klimaatToSave.setKlimaatSensor(klimaatSensor);
                klimaatRepository.save(klimaatToSave);
            }
        }
        receivedInLastQuarter.clear();
    }

    public KlimaatSensor getKlimaatSensorByCode(String klimaatSensorCode) {
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode);
    }

    public List<Klimaat> getInPeriod(String klimaatSensorCode, Date from, Date to) {
        if (to.before(new Date())) {
            return klimaatServiceCached.getInPeriod(klimaatSensorCode, from, to);
        } else {
            return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode, from, to);
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
        LOGGER.info("getMostRecent()");
        return klimaatRepository.getMostRecent();
    }

    public void add(Klimaat klimaat) {
        LOGGER.info("Recieved klimaat");
        if (!receivedInLastQuarter.containsKey(klimaat.getKlimaatSensor().getCode())) {
            receivedInLastQuarter.put(klimaat.getKlimaatSensor().getCode(), new ArrayList<>());
        }
        receivedInLastQuarter.get(klimaat.getKlimaatSensor().getCode()).add(klimaat);
        eventPublisher.publishEvent(new UpdateEvent(klimaat));
    }

    public List<BigDecimal> getValidHumidities(List<Klimaat> klimaatList) {
        return klimaatList.stream()
                .filter(klimaat -> klimaat.getLuchtvochtigheid() != null && !ZERO.equals(klimaat.getLuchtvochtigheid()))
                .map(Klimaat::getLuchtvochtigheid)
                .collect(toList());
    }

    private List<BigDecimal> getValidTemperatures(List<Klimaat> klimaatList) {
        return klimaatList.stream()
                .filter(klimaat -> klimaat.getTemperatuur() != null && !ZERO.equals(klimaat.getTemperatuur()))
                .map(Klimaat::getTemperatuur)
                .collect(toList());
    }

    public List<Klimaat> getHighest(SensorType sensortype, Date from, Date to, int limit) {
        switch (sensortype) {
            case TEMPERATUUR:
                return getHighestTemperature(from, to, limit);
            case LUCHTVOCHTIGHEID:
                return getHighestHumidity(from, to, limit);
            default:
                return emptyList();
        }
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
        return klimaatRepository.getPeakLowTemperatureDates(from, to, limit)
                    .stream()
                    .map(klimaatRepository::firstLowestTemperatureOnDay)
                    .collect(toList());
    }

    private List<Klimaat> getLowestHumidity(Date from, Date to, int limit) {
        return klimaatRepository.getPeakLowHumidityDates(from, to, limit)
                    .stream()
                    .map(klimaatRepository::firstLowestHumidityOnDay)
                    .collect(toList());
    }

    private BigDecimal getAverage(List<BigDecimal> decimals) {
        BigDecimal average = null;
        if (!decimals.isEmpty()) {
            BigDecimal total = decimals.stream().reduce(ZERO, BigDecimal::add);
            average = total.divide(BigDecimal.valueOf(decimals.size()), RoundingMode.HALF_UP);
        }
        return average;
    }

    private List<Klimaat> getHighestTemperature(Date from, Date to, int limit) {
        return klimaatRepository.getPeakHighTemperatureDates(from, to, limit)
                .stream()
                .map(klimaatRepository::firstHighestTemperatureOnDay)
                .collect(toList());
    }

    private List<Klimaat> getHighestHumidity(Date from, Date to, int limit) {
        return klimaatRepository.getPeakHighHumidityDates(from, to, limit)
                .stream()
                .map(klimaatRepository::firstHighestHumidityOnDay)
                .collect(toList());
    }
}
