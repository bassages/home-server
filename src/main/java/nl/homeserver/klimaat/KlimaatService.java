package nl.homeserver.klimaat;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nl.homeserver.DatePeriod;

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

    @Autowired
    private KlimaatService klimaatServiceProxyWithEnabledCaching; // Needed to make use of use caching annotations

    private final KlimaatRepos klimaatRepository;
    private final KlimaatSensorRepository klimaatSensorRepository;
    private final KlimaatSensorValueTrendService klimaatSensorValueTrendService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Clock clock;

    public KlimaatService(KlimaatRepos klimaatRepository, KlimaatSensorRepository klimaatSensorRepository,
            KlimaatSensorValueTrendService klimaatSensorValueTrendService, SimpMessagingTemplate messagingTemplate, Clock clock) {

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
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode)
                                      .orElseGet(() -> createKlimaatSensor(klimaatSensorCode));
    }

    private KlimaatSensor createKlimaatSensor(String klimaatSensorCode) {
        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode(klimaatSensorCode);
        return klimaatSensorRepository.save(klimaatSensor);
    }

    public Optional<KlimaatSensor> getKlimaatSensorByCode(String klimaatSensorCode) {
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode);
    }

    public List<Klimaat> getInPeriod(String klimaatSensorCode, DatePeriod period) {
        LocalDate today = LocalDate.now(clock);

        if (period.getEndDate().isBefore(today)) {
            return klimaatServiceProxyWithEnabledCaching.getPotentiallyCachedAllInPeriod(klimaatSensorCode, period);
        } else {
            return this.getNotCachedAllInPeriod(klimaatSensorCode, period);
        }
    }

    private BigDecimal getAverage(SensorType sensortype, DatePeriod period) {
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
        List<Klimaat> recentlyReceivedKlimaatForSensor = recentlyReceivedKlimaatsPerKlimaatSensorCode.get(klimaatSensorCode);
        return getLast(recentlyReceivedKlimaatForSensor).map(this::mapToRealtimeKlimaat)
                                                        .orElse(null);
    }

    private Optional<Klimaat> getLast(List<Klimaat> klimaats) {
        if (isEmpty(klimaats)) {
            return Optional.empty();
        }
        return Optional.of(klimaats.get(klimaats.size() - 1));
    }

    public void add(Klimaat klimaat) {
        recentlyReceivedKlimaatsPerKlimaatSensorCode.computeIfAbsent(klimaat.getKlimaatSensor().getCode(), klimaatSensorCode -> new ArrayList<>()).add(klimaat);
        publishEvent(klimaat);
        cleanUpRecentlyReceivedKlimaatsPerSensorCode();
    }

    public List<List<GemiddeldeKlimaatPerMaand>> getAverage(SensorType sensortype, int[] years) {
        return IntStream.of(years).mapToObj(jaar ->
                IntStream.rangeClosed(1, Month.values().length)
                        .mapToObj(maand -> getAverageInMonthOfYear(sensortype, YearMonth.of(jaar, maand)))
                        .collect(toList())).collect(toList());
    }


    private GemiddeldeKlimaatPerMaand getAverageInMonthOfYear(SensorType sensortype, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = from.plusMonths(1);
        DatePeriod period = aPeriodWithToDate(from, to);
        return new GemiddeldeKlimaatPerMaand(from, getAverage(sensortype, period));
    }

    private List<BigDecimal> getValidHumidities(List<Klimaat> klimaatList) {
        return klimaatList.stream()
                          .filter(klimaat -> klimaat.getLuchtvochtigheid() != null && klimaat.getLuchtvochtigheid().compareTo(ZERO) != 0)
                          .map(Klimaat::getLuchtvochtigheid)
                          .collect(toList());
    }

    private List<BigDecimal> getValidTemperatures(List<Klimaat> klimaatList) {
        return klimaatList.stream()
                          .filter(klimaat -> klimaat.getTemperatuur() != null && klimaat.getTemperatuur().compareTo(ZERO) != 0)
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

    private List<Klimaat> getNotCachedAllInPeriod(String klimaatSensorCode, DatePeriod period) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode,
                                                                                            period.toDateTimePeriod().getStartDateTime(),
                                                                                            period.toDateTimePeriod().getEndDateTime());
    }

    @Cacheable(cacheNames = "klimaatInPeriod")
    public List<Klimaat> getPotentiallyCachedAllInPeriod(String klimaatSensorCode, DatePeriod period) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode,
                                                                                            period.toDateTimePeriod().getStartDateTime(),
                                                                                            period.toDateTimePeriod().getEndDateTime());
    }
}
