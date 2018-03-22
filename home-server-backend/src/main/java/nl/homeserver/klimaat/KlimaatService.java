package nl.homeserver.klimaat;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalDateTime.now;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.klimaat.SensorType.LUCHTVOCHTIGHEID;
import static nl.homeserver.klimaat.SensorType.TEMPERATUUR;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import nl.homeserver.DatePeriod;

@Slf4j
@Service
public class KlimaatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KlimaatService.class);

    protected static final String REALTIME_KLIMAAT_TOPIC = "/topic/klimaat";

    private static final int NR_OF_MINUTES_TO_DETERMINE_TREND_FOR = 18;
    private static final int NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR = 15;

    private static final String EVERY_15_MINUTES_PAST_THE_HOUR = "0 0/" + NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR + " * * * ?";

    private static final int TEMPERATURE_SCALE = 2;
    private static final int HUMIDITY_SCALE = 1;

    private final Map<String, List<Klimaat>> recentlyReceivedKlimaatsPerKlimaatSensorCode = new ConcurrentHashMap<>();

    // Needed to make use of use caching annotations
    @Autowired
    private KlimaatService klimaatServiceProxyWithEnabledCaching;

    private final KlimaatRepos klimaatRepository;
    private final KlimaatSensorRepository klimaatSensorRepository;
    private final KlimaatSensorValueTrendService klimaatSensorValueTrendService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;

    public KlimaatService(final KlimaatRepos klimaatRepository,
                          final KlimaatSensorRepository klimaatSensorRepository,
                          final KlimaatSensorValueTrendService klimaatSensorValueTrendService,
                          final SimpMessagingTemplate messagingTemplate,
                          final Clock clock) {
        this.klimaatRepository = klimaatRepository;
        this.klimaatSensorRepository = klimaatSensorRepository;
        this.klimaatSensorValueTrendService = klimaatSensorValueTrendService;
        this.messagingTemplate = messagingTemplate;
        this.clock = clock;
    }

    private void cleanUpRecentlyReceivedKlimaatsPerSensorCode() {
        int maxNrOfMinutes = IntStream.of(NR_OF_MINUTES_TO_DETERMINE_TREND_FOR, NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR).max().getAsInt();
        LocalDateTime cleanUpAllBefore = now(clock).minusMinutes(maxNrOfMinutes);
        LOGGER.info("cleanUpRecentlyReceivedKlimaats before {}", cleanUpAllBefore);
        recentlyReceivedKlimaatsPerKlimaatSensorCode.values().forEach(klimaats -> klimaats.removeIf(klimaat -> klimaat.getDatumtijd().isBefore(cleanUpAllBefore)));
    }

    @Scheduled(cron = EVERY_15_MINUTES_PAST_THE_HOUR)
    public void save() {
        LocalDateTime referenceDateTime = now(clock).truncatedTo(ChronoUnit.MINUTES);
        recentlyReceivedKlimaatsPerKlimaatSensorCode
                .forEach((klimaatSensorCode, klimaats) -> this.saveKlimaatWithAveragedRecentSensorValues(referenceDateTime, klimaatSensorCode));
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

        if (Stream.of(averageTemperature, averageHumidity).anyMatch(Objects::nonNull)) {
            Klimaat klimaatToSave = new Klimaat();
            klimaatToSave.setDatumtijd(referenceDateTime);
            klimaatToSave.setTemperatuur(averageTemperature);
            klimaatToSave.setLuchtvochtigheid(averageHumidity);
            klimaatToSave.setKlimaatSensor(klimaatSensor);
            klimaatRepository.save(klimaatToSave);
        }
    }

    private List<Klimaat> getKlimaatsReceivedInLastNumberOfMinutes(String klimaatSensorCode, int nrOfMinutes) {
        LocalDateTime from = now(clock).minusMinutes(nrOfMinutes);
        return recentlyReceivedKlimaatsPerKlimaatSensorCode.getOrDefault(klimaatSensorCode, new ArrayList<>()).stream()
                                                           .filter(klimaat -> klimaat.getDatumtijd().isAfter(from))
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

    private KlimaatSensor getOrCreateIfNonExists(String klimaatSensorCode) {
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode)
                                      .orElseGet(() -> createKlimaatSensor(klimaatSensorCode));
    }

    private KlimaatSensor createKlimaatSensor(String klimaatSensorCode) {
        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode(klimaatSensorCode);
        klimaatSensor.setOmschrijving(null);
        return klimaatSensorRepository.save(klimaatSensor);
    }

    public List<Klimaat> getInPeriod(String klimaatSensorCode, DatePeriod period) {
        LocalDate today = LocalDate.now(clock);

        if (period.getEndDate().isBefore(today)) {
            return klimaatServiceProxyWithEnabledCaching.getPotentiallyCachedAllInPeriod(klimaatSensorCode, period);
        } else {
            return this.getNotCachedAllInPeriod(klimaatSensorCode, period);
        }
    }

    private BigDecimal getAverage(String sensorCode, SensorType sensorType, DatePeriod period) {
        if (sensorType == TEMPERATUUR) {
            return klimaatRepository.getAverageTemperatuur(sensorCode, period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay());
        } else if (sensorType == LUCHTVOCHTIGHEID) {
            return klimaatRepository.getAverageLuchtvochtigheid(sensorCode, period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay());
        } else {
            throw new IllegalArgumentException(createUnexpectedSensorTypeErrorMessage(sensorType));
        }
    }

    public RealtimeKlimaat getMostRecent(String klimaatSensorCode) {
        List<Klimaat> recentlyReceivedKlimaatForSensor = recentlyReceivedKlimaatsPerKlimaatSensorCode.getOrDefault(klimaatSensorCode, new ArrayList<>());
        return getMostRecent(recentlyReceivedKlimaatForSensor).map(this::mapToRealtimeKlimaat)
                                                              .orElse(null);
    }

    private Optional<Klimaat> getMostRecent(List<Klimaat> klimaats) {
        return klimaats.stream().max(comparing(Klimaat::getDatumtijd));
    }

    public void add(Klimaat klimaat) {
        publishEvent(klimaat);
        recentlyReceivedKlimaatsPerKlimaatSensorCode.computeIfAbsent(klimaat.getKlimaatSensor().getCode(), klimaatSensorCode -> new ArrayList<>()).add(klimaat);
        cleanUpRecentlyReceivedKlimaatsPerSensorCode();
    }

    public List<List<GemiddeldeKlimaatPerMaand>> getAveragePerMonthInYears(String sensorCode, SensorType sensorType, int[] years) {
        return IntStream.of(years)
                        .mapToObj(Year::of)
                        .map(year -> getAveragePerMonthInYear(sensorCode, sensorType, year))
                        .collect(toList());
    }

    private List<GemiddeldeKlimaatPerMaand> getAveragePerMonthInYear(String sensorCode, SensorType sensorType, Year year) {
        return IntStream.rangeClosed(1, Month.values().length)
                        .mapToObj(maand -> getAverageInMonthOfYear(sensorCode, sensorType, YearMonth.of(year.getValue(), maand)))
                        .collect(toList());
    }

    private GemiddeldeKlimaatPerMaand getAverageInMonthOfYear(String sensorCode, SensorType sensorType, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = from.plusMonths(1);
        DatePeriod period = aPeriodWithToDate(from, to);
        return new GemiddeldeKlimaatPerMaand(from, getAverage(sensorCode, sensorType, period));
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

    public List<Klimaat> getHighest(String sensorCode, SensorType sensorType, DatePeriod period, int limit) {
        if (sensorType == TEMPERATUUR) {
            return getHighestTemperature(sensorCode, period, limit);
        } else if (sensorType == LUCHTVOCHTIGHEID) {
            return getHighestHumidity(sensorCode, period, limit);
        } else {
            throw new IllegalArgumentException(createUnexpectedSensorTypeErrorMessage(sensorType));
        }
    }

    public List<Klimaat> getLowest(String sensorCode, SensorType sensorType, DatePeriod period, int limit) {
        if (sensorType == TEMPERATUUR) {
            return getLowestTemperature(sensorCode, period, limit);
        } else if (sensorType == LUCHTVOCHTIGHEID) {
            return getLowestHumidity(sensorCode, period, limit);
        } else {
            throw new IllegalArgumentException(createUnexpectedSensorTypeErrorMessage(sensorType));
        }
    }

    private String createUnexpectedSensorTypeErrorMessage(SensorType sensorType) {
        return format("Unexpected SensorType [%s]", sensorType);
    }

    private List<Klimaat> getLowestTemperature(String sensorCode, DatePeriod period, int limit) {
        return klimaatRepository.getPeakLowTemperatureDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestLowestTemperatureOnDay(sensorCode, day))
                                .collect(toList());
    }

    private List<Klimaat> getLowestHumidity(String sensorCode, DatePeriod period, int limit) {
        return klimaatRepository.getPeakLowHumidityDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestLowestHumidityOnDay(sensorCode, day))
                                .collect(toList());
    }

    private List<Klimaat> getHighestTemperature(String sensorCode, DatePeriod period, int limit) {
        return klimaatRepository.getPeakHighTemperatureDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestHighestTemperatureOnDay(sensorCode, day))
                                .collect(toList());
    }

    private List<Klimaat> getHighestHumidity(String sensorCode, DatePeriod period, int limit) {
        return klimaatRepository.getPeakHighHumidityDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestHighestHumidityOnDay(sensorCode, day))
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
                                                                                            period.toDateTimePeriod().getFromDateTime(),
                                                                                            period.toDateTimePeriod().getEndDateTime());
    }

    @Cacheable(cacheNames = "klimaatInPeriod")
    public List<Klimaat> getPotentiallyCachedAllInPeriod(String klimaatSensorCode, DatePeriod period) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode,
                                                                                            period.toDateTimePeriod().getFromDateTime(),
                                                                                            period.toDateTimePeriod().getEndDateTime());
    }

    public Optional<KlimaatSensor> getKlimaatSensorByCode(String klimaatSensorCode) {
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode);
    }

    public List<KlimaatSensor> getAllKlimaatSensors() {
        return klimaatSensorRepository.findAll();
    }
}
