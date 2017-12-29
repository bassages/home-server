package nl.wiegman.home.klimaat;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DatePeriod;
import nl.wiegman.home.DateTimePeriod;

@Service
public class KlimaatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KlimaatService.class);

    private static final String REALTIME_KLIMAAT_TOPIC = "/topic/klimaat";

    private static final int NR_OF_MINUTES_TO_DETERMINE_TREND_FOR = 18;
    private static final int NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR = 15;

    private static final String EVERY_15_MINUTES_PAST_THE_HOUR = "0 0/" + NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR + " * * * ?";

    private static final int TEMPERATURE_SCALE = 2;
    private static final int HUMIDITY_SCALE = 1;

    private final Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = new ConcurrentHashMap<>();

    private final KlimaatServiceCached klimaatServiceCached;
    private final KlimaatRepos klimaatRepository;
    private final KlimaatSensorRepository klimaatSensorRepository;

    private final KlimaatSensorValueTrendService klimaatSensorValueTrendService;

    private final SimpMessagingTemplate messagingTemplate;

    private final Clock clock;

    @Autowired
    public KlimaatService(KlimaatServiceCached klimaatServiceCached, KlimaatRepos klimaatRepository, KlimaatSensorRepository klimaatSensorRepository,
            KlimaatSensorValueTrendService klimaatSensorValueTrendService, SimpMessagingTemplate messagingTemplate, Clock clock) {

        this.klimaatServiceCached = klimaatServiceCached;
        this.klimaatRepository = klimaatRepository;
        this.klimaatSensorRepository = klimaatSensorRepository;
        this.klimaatSensorValueTrendService = klimaatSensorValueTrendService;
        this.messagingTemplate = messagingTemplate;
        this.clock = clock;
    }

    private void cleanUpRecentlyReceivedKlimaatsPerSensorCode() {
        int maxNrOfMinutes = IntStream.of(NR_OF_MINUTES_TO_DETERMINE_TREND_FOR, NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR).max().getAsInt();
        LocalDateTime cleanUpAllBefore = LocalDateTime.now().minusMinutes(maxNrOfMinutes);
        LOGGER.info("cleanUpRecentlyReceivedKlimaats before {}", cleanUpAllBefore);
        recentlyReceivedKlimaatsPerKlimaatSensorCode.values().forEach(klimaats -> klimaats.removeIf(klimaat -> klimaat.getDatumtijd().isBefore(cleanUpAllBefore)));
    }

    @Scheduled(cron = EVERY_15_MINUTES_PAST_THE_HOUR)
    public void save() {
        LocalDateTime referenceDateTime = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        recentlyReceivedKlimaatsPerKlimaatSensorCode
                .forEach((klimaatSensorCode, klimaats) -> this.saveKlimaatWithAveragedRecentSensorValues(referenceDateTime, klimaatSensorCode));
    }

    private List<Klimaat> getKlimaatsReceivedInLastNumberOfMinutes(String klimaatSensorCode, int nrOfMinutes) {
        LocalDateTime from = LocalDateTime.now(clock).minusMinutes(nrOfMinutes);
        return recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensorCode).stream()
                .filter(klimaat -> klimaat.getDatumtijd().isAfter(from))
                .collect(toList());
    }

    private void saveKlimaatWithAveragedRecentSensorValues(LocalDateTime referenceDateTime, String klimaatSensorCode) {
        List<Klimaat> klimaatsReceivedInLastNumberOfMinutes = getKlimaatsReceivedInLastNumberOfMinutes(klimaatSensorCode,
                NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR);

        List<BigDecimal> validTemperaturesFromLastQuarter = getValidTemperatures(klimaatsReceivedInLastNumberOfMinutes);
        List<BigDecimal> validHumiditiesFromLastQuarter = getValidHumidities(klimaatsReceivedInLastNumberOfMinutes);

        KlimaatSensor klimaatSensor = getOrCreateIfNonExists(klimaatSensorCode);

        BigDecimal averageTemperature = getAverage(validTemperaturesFromLastQuarter);
        if (averageTemperature != null) {
            averageTemperature = averageTemperature.setScale(TEMPERATURE_SCALE, HALF_UP);
        }

        BigDecimal averageHumidity = getAverage(validHumiditiesFromLastQuarter);
        if (averageHumidity != null) {
            averageHumidity = averageHumidity.setScale(HUMIDITY_SCALE, HALF_UP);
        }

        if (averageTemperature != null || averageHumidity != null) {
            Klimaat klimaatToSave = new Klimaat();
            klimaatToSave.setDatumtijd(referenceDateTime);
            klimaatToSave.setTemperatuur(averageTemperature);
            klimaatToSave.setLuchtvochtigheid(averageHumidity);
            klimaatToSave.setKlimaatSensor(klimaatSensor);
            klimaatRepository.save(klimaatToSave);
        }
    }

    private KlimaatSensor getOrCreateIfNonExists(String klimaatSensorCode) {
        KlimaatSensor klimaatSensor = klimaatSensorRepository.findFirstByCode(klimaatSensorCode);
        if (klimaatSensor == null) {
            klimaatSensor = new KlimaatSensor();
            klimaatSensor.setCode(klimaatSensorCode);
            klimaatSensor = klimaatSensorRepository.save(klimaatSensor);
        }
        return klimaatSensor;
    }

    public KlimaatSensor getKlimaatSensorByCode(String klimaatSensorCode) {
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode);
    }

    public List<Klimaat> getInPeriod(String klimaatSensorCode, DateTimePeriod period) {
        LocalDate today = LocalDate.now(clock);

        if (period.getToDateTime().isBefore(today.atStartOfDay())) {
            return klimaatServiceCached.getInPeriod(klimaatSensorCode, period);
        } else {
            return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode, period.getFromDateTime(), period.getToDateTime());
        }
    }

    public BigDecimal getAverage(SensorType sensortype, DatePeriod period) {
        switch (sensortype) {
            case TEMPERATUUR:
                return klimaatRepository.getAverageTemperatuur(period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay());
            case LUCHTVOCHTIGHEID:
                return klimaatRepository.getAverageLuchtvochtigheid(period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay());
            default:
                return null;
        }
    }

    public RealtimeKlimaat getMostRecent(String klimaatSensorCode) {
        return getLast(recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensorCode))
                .map(this::mapToRealtimeKlimaat)
                .orElse(null);
    }

    private Optional<Klimaat> getLast(List<Klimaat> klimaats) {
        if (isNotEmpty(klimaats)) {
            return Optional.of(klimaats.get(klimaats.size() - 1));
        } else {
            return Optional.empty();
        }
    }

    public void add(Klimaat klimaat) {
        recentlyReceivedKlimaatsPerKlimaatSensorCode.computeIfAbsent(klimaat.getKlimaatSensor().getCode(), klimaatSensorCode -> new ArrayList<>()).add(klimaat);
        publishEvent(klimaat);
        cleanUpRecentlyReceivedKlimaatsPerSensorCode();
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

    public List<Klimaat> getHighest(SensorType sensortype, DatePeriod period, int limit) {
        switch (sensortype) {
            case TEMPERATUUR:
                return getHighestTemperature(period, limit);
            case LUCHTVOCHTIGHEID:
                return getHighestHumidity(period, limit);
            default:
                return emptyList();
        }
    }

    public List<Klimaat> getLowest(SensorType sensortype, DatePeriod period, int limit) {
        switch (sensortype) {
            case TEMPERATUUR:
                return getLowestTemperature(period, limit);
            case LUCHTVOCHTIGHEID:
                return getLowestHumidity(period, limit);
            default:
                return null;
        }
    }

    private List<Klimaat> getLowestTemperature(DatePeriod period, int limit) {
        return klimaatRepository.getPeakLowTemperatureDates(period.getFromDate(), period.getToDate(), limit)
                    .stream()
                    .map(java.sql.Date::toLocalDate)
                    .map(klimaatRepository::firstLowestTemperatureOnDay)
                    .collect(toList());
    }

    private List<Klimaat> getLowestHumidity(DatePeriod period, int limit) {
        return klimaatRepository.getPeakLowHumidityDates(period.getFromDate(), period.getToDate(), limit)
                    .stream()
                    .map(java.sql.Date::toLocalDate)
                    .map(klimaatRepository::firstLowestHumidityOnDay)
                    .collect(toList());
    }

    private BigDecimal getAverage(List<BigDecimal> decimals) {
        BigDecimal average = null;
        if (!decimals.isEmpty()) {
            BigDecimal total = decimals.stream().reduce(ZERO, BigDecimal::add);
            average = total.divide(BigDecimal.valueOf(decimals.size()), HALF_UP);
        }
        return average;
    }

    private List<Klimaat> getHighestTemperature(DatePeriod period, int limit) {
        return klimaatRepository.getPeakHighTemperatureDates(period.getFromDate(), period.getToDate(), limit)
                .stream()
                .map(java.sql.Date::toLocalDate)
                .map(klimaatRepository::firstHighestTemperatureOnDay)
                .collect(toList());
    }

    private List<Klimaat> getHighestHumidity(DatePeriod period, int limit) {
        return klimaatRepository.getPeakHighHumidityDates(period.getFromDate(), period.getToDate(), limit)
                .stream()
                .map(klimaatRepository::firstHighestHumidityOnDay)
                .collect(toList());
    }

    private void publishEvent(Klimaat klimaat) {
        RealtimeKlimaat realtimeKlimaat = mapToRealtimeKlimaat(klimaat);
        messagingTemplate.convertAndSend(REALTIME_KLIMAAT_TOPIC, realtimeKlimaat);
    }

    private RealtimeKlimaat mapToRealtimeKlimaat(Klimaat klimaat) {
        RealtimeKlimaat realtimeKlimaat = new RealtimeKlimaat();
        realtimeKlimaat.setDatumtijd(klimaat.getDatumtijd());
        realtimeKlimaat.setLuchtvochtigheid(klimaat.getLuchtvochtigheid());
        realtimeKlimaat.setTemperatuur(klimaat.getTemperatuur());

        List<Klimaat> klimaatsToDetermineTrendFor = getKlimaatsReceivedInLastNumberOfMinutes(klimaat.getKlimaatSensor().getCode(),
                NR_OF_MINUTES_TO_DETERMINE_TREND_FOR);
        realtimeKlimaat.setTemperatuurTrend(klimaatSensorValueTrendService.determineValueTrend(klimaatsToDetermineTrendFor, Klimaat::getTemperatuur));
        realtimeKlimaat.setLuchtvochtigheidTrend(klimaatSensorValueTrendService.determineValueTrend(klimaatsToDetermineTrendFor, Klimaat::getLuchtvochtigheid));

        return realtimeKlimaat;
    }
}
