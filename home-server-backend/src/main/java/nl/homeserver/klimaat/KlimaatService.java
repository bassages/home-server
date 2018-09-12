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

import javax.annotation.Nullable;

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

    protected static final String REALTIME_KLIMAAT_TOPIC = "/topic/klimaat";

    private static final int NR_OF_MINUTES_TO_DETERMINE_TREND_FOR = 18;
    private static final int NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR = 15;

    private static final String EVERY_15_MINUTES_PAST_THE_HOUR = "0 0/" + NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR + " * * * ?";

    private static final int TEMPERATURE_SCALE = 2;
    private static final int HUMIDITY_SCALE = 1;

    private static final String CACHE_NAME_AVERAGE_KLIMAAT_IN_MONTH = "averageKlimaatInMonth";

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
        final int maxNrOfMinutes = IntStream.of(NR_OF_MINUTES_TO_DETERMINE_TREND_FOR, NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR).max().getAsInt();
        final LocalDateTime cleanUpAllBefore = now(clock).minusMinutes(maxNrOfMinutes);
        LOGGER.info("cleanUpRecentlyReceivedKlimaats before {}", cleanUpAllBefore);
        recentlyReceivedKlimaatsPerKlimaatSensorCode.values()
                                                    .forEach(klimaats -> klimaats.removeIf(klimaat -> klimaat.getDatumtijd().isBefore(cleanUpAllBefore)));
    }

    @Scheduled(cron = EVERY_15_MINUTES_PAST_THE_HOUR)
    public void save() {
        final LocalDateTime referenceDateTime = now(clock).truncatedTo(ChronoUnit.MINUTES);
        recentlyReceivedKlimaatsPerKlimaatSensorCode
                .forEach((klimaatSensorCode, klimaats) -> this.saveKlimaatWithAveragedRecentSensorValues(referenceDateTime, klimaatSensorCode));
    }

    private void saveKlimaatWithAveragedRecentSensorValues(final LocalDateTime referenceDateTime, final String klimaatSensorCode) {
        final List<Klimaat> klimaatsReceivedInLastNumberOfMinutes = getKlimaatsReceivedInLastNumberOfMinutes(klimaatSensorCode,
                NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR);

        final List<BigDecimal> validTemperaturesFromLastQuarter = getValidTemperatures(klimaatsReceivedInLastNumberOfMinutes);
        final List<BigDecimal> validHumiditiesFromLastQuarter = getValidHumidities(klimaatsReceivedInLastNumberOfMinutes);

        final KlimaatSensor klimaatSensor = getOrCreateIfNonExists(klimaatSensorCode);

        BigDecimal averageTemperature = getAverage(validTemperaturesFromLastQuarter);
        if (averageTemperature != null) {
            averageTemperature = averageTemperature.setScale(TEMPERATURE_SCALE, HALF_UP);
        }

        BigDecimal averageHumidity = getAverage(validHumiditiesFromLastQuarter);
        if (averageHumidity != null) {
            averageHumidity = averageHumidity.setScale(HUMIDITY_SCALE, HALF_UP);
        }

        if (Stream.of(averageTemperature, averageHumidity).anyMatch(Objects::nonNull)) {
            final Klimaat klimaatToSave = new Klimaat();
            klimaatToSave.setDatumtijd(referenceDateTime);
            klimaatToSave.setTemperatuur(averageTemperature);
            klimaatToSave.setLuchtvochtigheid(averageHumidity);
            klimaatToSave.setKlimaatSensor(klimaatSensor);
            klimaatRepository.save(klimaatToSave);
        }
    }

    private List<Klimaat> getKlimaatsReceivedInLastNumberOfMinutes(final String klimaatSensorCode, final int nrOfMinutes) {
        final LocalDateTime from = now(clock).minusMinutes(nrOfMinutes);
        return recentlyReceivedKlimaatsPerKlimaatSensorCode.getOrDefault(klimaatSensorCode, new ArrayList<>()).stream()
                                                           .filter(klimaat -> klimaat.getDatumtijd().isAfter(from))
                                                           .collect(toList());
    }

    private BigDecimal getAverage(final List<BigDecimal> decimals) {
        BigDecimal average = null;
        if (!decimals.isEmpty()) {
            final BigDecimal total = decimals.stream().reduce(ZERO, BigDecimal::add);
            average = total.divide(BigDecimal.valueOf(decimals.size()), HALF_UP);
        }
        return average;
    }

    private KlimaatSensor getOrCreateIfNonExists(final String klimaatSensorCode) {
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode)
                                      .orElseGet(() -> createKlimaatSensor(klimaatSensorCode));
    }

    private KlimaatSensor createKlimaatSensor(final String klimaatSensorCode) {
        final KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode(klimaatSensorCode);
        klimaatSensor.setOmschrijving(null);
        return klimaatSensorRepository.save(klimaatSensor);
    }

    public List<Klimaat> getInPeriod(final String klimaatSensorCode, final DatePeriod period) {
        final LocalDate today = LocalDate.now(clock);

        if (period.getEndDate().isBefore(today)) {
            return klimaatServiceProxyWithEnabledCaching.getPotentiallyCachedAllInPeriod(klimaatSensorCode, period);
        } else {
            return this.getNotCachedAllInPeriod(klimaatSensorCode, period);
        }
    }

    private BigDecimal getAverage(final String sensorCode, final SensorType sensorType, final DatePeriod period) {
        if (sensorType == TEMPERATUUR) {
            return klimaatRepository.getAverageTemperatuur(sensorCode,
                                                           period.getFromDate().atStartOfDay(),
                                                           period.getToDate().atStartOfDay());
        } else if (sensorType == LUCHTVOCHTIGHEID) {
            return klimaatRepository.getAverageLuchtvochtigheid(sensorCode,
                                                                period.getFromDate().atStartOfDay(),
                                                                period.getToDate().atStartOfDay());
        } else {
            throw new IllegalArgumentException(createUnexpectedSensorTypeErrorMessage(sensorType));
        }
    }

    public RealtimeKlimaat getMostRecent(final String klimaatSensorCode) {
        final List<Klimaat> recentlyReceivedKlimaatForSensor = recentlyReceivedKlimaatsPerKlimaatSensorCode.getOrDefault(klimaatSensorCode, new ArrayList<>());
        return getMostRecent(recentlyReceivedKlimaatForSensor).map(this::mapToRealtimeKlimaat)
                                                              .orElse(null);
    }

    private Optional<Klimaat> getMostRecent(final List<Klimaat> klimaats) {
        return klimaats.stream().max(comparing(Klimaat::getDatumtijd));
    }

    public void add(final Klimaat klimaat) {
        if (klimaat.getDatumtijd() == null) {
            klimaat.setDatumtijd(now(clock));
        }
        publishEvent(klimaat);
        recentlyReceivedKlimaatsPerKlimaatSensorCode.computeIfAbsent(klimaat.getKlimaatSensor().getCode(),
                                                                     klimaatSensorCode -> new ArrayList<>()).add(klimaat);
        cleanUpRecentlyReceivedKlimaatsPerSensorCode();
    }

    public List<List<GemiddeldeKlimaatPerMaand>> getAveragePerMonthInYears(final String sensorCode,
                                                                           final SensorType sensorType,
                                                                           final int[] years) {
        return IntStream.of(years)
                        .mapToObj(Year::of)
                        .map(year -> getAveragePerMonthInYear(sensorCode, sensorType, year))
                        .collect(toList());
    }

    private List<GemiddeldeKlimaatPerMaand> getAveragePerMonthInYear(final String sensorCode,
                                                                     final SensorType sensorType,
                                                                     final Year year) {
        return IntStream.rangeClosed(1, Month.values().length)
                        .mapToObj(month -> YearMonth.of(year.getValue(), month))
                        .map(yearMonth -> getAverageInMonthOfYear(sensorCode, sensorType, yearMonth))
                        .collect(toList());
    }

    private GemiddeldeKlimaatPerMaand getAverageInMonthOfYear(final String sensorCode,
                                                              final SensorType sensorType,
                                                              final YearMonth yearMonth) {
        if (yearMonth.isBefore(YearMonth.now(clock))) {
            return klimaatServiceProxyWithEnabledCaching.getPotentiallyCachedAverageInMonthOfYear(sensorCode,
                                                                                                  sensorType,
                                                                                                  yearMonth);
        } else if (yearMonth.isAfter(YearMonth.now(clock))) {
            return new GemiddeldeKlimaatPerMaand(yearMonth.atDay(1), null);
        } else {
            return getNonCachedAverageInMonthOfYear(sensorCode, sensorType, yearMonth);
        }
    }

    @Cacheable(cacheNames = CACHE_NAME_AVERAGE_KLIMAAT_IN_MONTH)
    public GemiddeldeKlimaatPerMaand getPotentiallyCachedAverageInMonthOfYear(final String sensorCode,
                                                                              final SensorType sensorType,
                                                                              final YearMonth yearMonth) {
        return getNonCachedAverageInMonthOfYear(sensorCode, sensorType, yearMonth);
    }

    private GemiddeldeKlimaatPerMaand getNonCachedAverageInMonthOfYear(final String sensorCode,
                                                                       final SensorType sensorType,
                                                                       final YearMonth yearMonth) {
        final LocalDate from = yearMonth.atDay(1);
        final LocalDate to = from.plusMonths(1);
        final DatePeriod period = aPeriodWithToDate(from, to);
        return new GemiddeldeKlimaatPerMaand(from, getAverage(sensorCode, sensorType, period));
    }

    private List<BigDecimal> getValidHumidities(final List<Klimaat> klimaatList) {
        return klimaatList.stream()
                          .filter(klimaat -> isValidKlimaatValue(klimaat.getLuchtvochtigheid()))
                          .map(Klimaat::getLuchtvochtigheid)
                          .collect(toList());
    }

    private List<BigDecimal> getValidTemperatures(final List<Klimaat> klimaatList) {
        return klimaatList.stream()
                          .filter(klimaat -> isValidKlimaatValue(klimaat.getTemperatuur()))
                          .map(Klimaat::getTemperatuur)
                          .collect(toList());
    }

    private boolean isValidKlimaatValue(@Nullable final BigDecimal value) {
        return value != null && value.compareTo(ZERO) != 0;
    }

    public List<Klimaat> getHighest(final String sensorCode,
                                    final SensorType sensorType,
                                    final DatePeriod period,
                                    final int limit) {
        if (sensorType == TEMPERATUUR) {
            return getHighestTemperature(sensorCode, period, limit);
        } else if (sensorType == LUCHTVOCHTIGHEID) {
            return getHighestHumidity(sensorCode, period, limit);
        } else {
            throw new IllegalArgumentException(createUnexpectedSensorTypeErrorMessage(sensorType));
        }
    }

    public List<Klimaat> getLowest(final String sensorCode, final SensorType sensorType, final DatePeriod period, final int limit) {
        if (sensorType == TEMPERATUUR) {
            return getLowestTemperature(sensorCode, period, limit);
        } else if (sensorType == LUCHTVOCHTIGHEID) {
            return getLowestHumidity(sensorCode, period, limit);
        } else {
            throw new IllegalArgumentException(createUnexpectedSensorTypeErrorMessage(sensorType));
        }
    }

    private String createUnexpectedSensorTypeErrorMessage(final SensorType sensorType) {
        return format("Unexpected SensorType [%s]", sensorType);
    }

    private List<Klimaat> getLowestTemperature(final String sensorCode, final DatePeriod period, final int limit) {
        return klimaatRepository.getPeakLowTemperatureDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestLowestTemperatureOnDay(sensorCode, day))
                                .collect(toList());
    }

    private List<Klimaat> getLowestHumidity(final String sensorCode, final DatePeriod period, final int limit) {
        return klimaatRepository.getPeakLowHumidityDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestLowestHumidityOnDay(sensorCode, day))
                                .collect(toList());
    }

    private List<Klimaat> getHighestTemperature(final String sensorCode, final DatePeriod period, final int limit) {
        return klimaatRepository.getPeakHighTemperatureDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestHighestTemperatureOnDay(sensorCode, day))
                                .collect(toList());
    }

    private List<Klimaat> getHighestHumidity(final String sensorCode, final DatePeriod period, final int limit) {
        return klimaatRepository.getPeakHighHumidityDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestHighestHumidityOnDay(sensorCode, day))
                                .collect(toList());
    }

    private void publishEvent(final Klimaat klimaat) {
        final RealtimeKlimaat realtimeKlimaat = mapToRealtimeKlimaat(klimaat);
        messagingTemplate.convertAndSend(REALTIME_KLIMAAT_TOPIC, realtimeKlimaat);
    }

    private RealtimeKlimaat mapToRealtimeKlimaat(final Klimaat klimaat) {
        final RealtimeKlimaat realtimeKlimaat = new RealtimeKlimaat();
        realtimeKlimaat.setDatumtijd(klimaat.getDatumtijd());
        realtimeKlimaat.setLuchtvochtigheid(klimaat.getLuchtvochtigheid());
        realtimeKlimaat.setTemperatuur(klimaat.getTemperatuur());
        realtimeKlimaat.setSensorCode(klimaat.getKlimaatSensor().getCode());

        final List<Klimaat> klimaatsToDetermineTrendFor = getKlimaatsReceivedInLastNumberOfMinutes(klimaat.getKlimaatSensor().getCode(),
                NR_OF_MINUTES_TO_DETERMINE_TREND_FOR);
        realtimeKlimaat.setTemperatuurTrend(klimaatSensorValueTrendService.determineValueTrend(klimaatsToDetermineTrendFor, Klimaat::getTemperatuur));
        realtimeKlimaat.setLuchtvochtigheidTrend(klimaatSensorValueTrendService.determineValueTrend(klimaatsToDetermineTrendFor, Klimaat::getLuchtvochtigheid));

        return realtimeKlimaat;
    }

    private List<Klimaat> getNotCachedAllInPeriod(final String klimaatSensorCode, final DatePeriod period) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode,
                                                                                            period.toDateTimePeriod().getFromDateTime(),
                                                                                            period.toDateTimePeriod().getEndDateTime());
    }

    @Cacheable(cacheNames = "klimaatInPeriod")
    public List<Klimaat> getPotentiallyCachedAllInPeriod(final String klimaatSensorCode, final DatePeriod period) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode,
                                                                                            period.toDateTimePeriod().getFromDateTime(),
                                                                                            period.toDateTimePeriod().getEndDateTime());
    }

    public Optional<KlimaatSensor> getKlimaatSensorByCode(final String klimaatSensorCode) {
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode);
    }

    public List<KlimaatSensor> getAllKlimaatSensors() {
        return klimaatSensorRepository.findAll();
    }

    public KlimaatSensor update(final KlimaatSensor klimaatSensor) {
        return klimaatSensorRepository.save(klimaatSensor);
    }

    public void delete(final KlimaatSensor klimaatSensor) {
        this.klimaatRepository.deleteByKlimaatSensorCode(klimaatSensor.getCode());
        this.klimaatSensorRepository.delete(klimaatSensor);
    }
}
