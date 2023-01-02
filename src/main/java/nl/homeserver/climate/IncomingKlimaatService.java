package nl.homeserver.climate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.util.Comparator.comparing;
import static nl.homeserver.climate.RealtimeKlimaat.aRealtimeKlimaat;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomingKlimaatService {
    static final String REALTIME_KLIMAAT_TOPIC = "/topic/klimaat";

    private static final int NR_OF_MINUTES_TO_DETERMINE_TREND_FOR = 18;
    private static final int NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR = 15;

    private static final String EVERY_X_MINUTES_PAST_THE_HOUR =
            "0 0/" + NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR + " * * * ?";

    private static final int TEMPERATURE_SCALE = 2;
    private static final int HUMIDITY_SCALE = 1;

    private final Map<String, List<Klimaat>> recentlyAddedKlimaatsPerKlimaatSensorCode = new ConcurrentHashMap<>();

    private final KlimaatSensorValueTrendService klimaatSensorValueTrendService;
    private final KlimaatSensorService klimaatSensorService;
    private final SimpMessagingTemplate messagingTemplate;
    private final KlimaatService klimaatService;
    private final Clock clock;

    @Scheduled(cron = EVERY_X_MINUTES_PAST_THE_HOUR)
    void save() {
        final LocalDateTime referenceDateTime = determineReferenceDateTime();
        recentlyAddedKlimaatsPerKlimaatSensorCode.forEach(
                (klimaatSensorCode, klimaats) ->
                        this.saveKlimaatWithAveragedRecentSensorValues(referenceDateTime, klimaatSensorCode));
    }

    private LocalDateTime determineReferenceDateTime() {
        final LocalDateTime now = now(clock);
        final LocalDateTime referenceDateTime;
        if (now.getSecond() < 30) {
            referenceDateTime = now.with(SECOND_OF_MINUTE, 0);
        } else {
            referenceDateTime = now.with(MINUTE_OF_HOUR, now.get(MINUTE_OF_HOUR) + 1L)
                                   .with(SECOND_OF_MINUTE, 0);
        }
        return referenceDateTime;
    }

    private void saveKlimaatWithAveragedRecentSensorValues(final LocalDateTime referenceDateTime,
                                                           final String klimaatSensorCode) {

        final List<Klimaat> klimaatsReceivedInLastNumberOfMinutes =
                getKlimaatsAddedInLastNumberOfMinutes(klimaatSensorCode, NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR);

        final List<BigDecimal> validTemperaturesInLastPeriod =
                getValidTemperatures(klimaatsReceivedInLastNumberOfMinutes);

        final List<BigDecimal> validHumiditiesFromLastPeriod =
                getValidHumidities(klimaatsReceivedInLastNumberOfMinutes);

        final KlimaatSensor klimaatSensor = klimaatSensorService.getOrCreateIfNonExists(klimaatSensorCode);

        BigDecimal averageTemperature = getAverage(validTemperaturesInLastPeriod);
        if (averageTemperature != null) {
            averageTemperature = averageTemperature.setScale(TEMPERATURE_SCALE, HALF_UP);
        }

        BigDecimal averageHumidity = getAverage(validHumiditiesFromLastPeriod);
        if (averageHumidity != null) {
            averageHumidity = averageHumidity.setScale(HUMIDITY_SCALE, HALF_UP);
        }

        if (Stream.of(averageTemperature, averageHumidity).anyMatch(Objects::nonNull)) {
            final Klimaat klimaatToSave = new Klimaat();
            klimaatToSave.setDatumtijd(referenceDateTime);
            klimaatToSave.setTemperatuur(averageTemperature);
            klimaatToSave.setLuchtvochtigheid(averageHumidity);
            klimaatToSave.setKlimaatSensor(klimaatSensor);
            klimaatService.save(klimaatToSave);
        }
    }

    private List<Klimaat> getKlimaatsAddedInLastNumberOfMinutes(
            final String klimaatSensorCode, final int nrOfMinutes) {

        final LocalDateTime from = now(clock).minusMinutes(nrOfMinutes);
        return recentlyAddedKlimaatsPerKlimaatSensorCode.getOrDefault(klimaatSensorCode, new ArrayList<>())
                                                        .stream()
                                                        .filter(klimaat -> klimaat.getDatumtijd().isAfter(from))
                                                        .toList();
    }

    @Nullable
    private BigDecimal getAverage(final List<BigDecimal> decimals) {
        BigDecimal average = null;
        if (!decimals.isEmpty()) {
            final BigDecimal total = decimals.stream().reduce(ZERO, BigDecimal::add);
            average = total.divide(BigDecimal.valueOf(decimals.size()), HALF_UP);
        }
        return average;
    }

    void add(final Klimaat klimaat) {
        if (klimaat.getDatumtijd() == null) {
            klimaat.setDatumtijd(now(clock));
        }
        publishEvent(klimaat);
        recentlyAddedKlimaatsPerKlimaatSensorCode.computeIfAbsent(klimaat.getKlimaatSensor().getCode(),
                                                                  klimaatSensorCode -> new ArrayList<>())
                                                 .add(klimaat);
        cleanUpRecentlyReceivedKlimaatsPerSensorCode();
    }

    private void cleanUpRecentlyReceivedKlimaatsPerSensorCode() {
        final int maxNrOfMinutes = IntStream.of(NR_OF_MINUTES_TO_DETERMINE_TREND_FOR,
                                                NR_OF_MINUTES_TO_SAVE_AVERAGE_KLIMAAT_FOR)
                                            .max()
                                            .getAsInt();
        final LocalDateTime cleanUpAllBefore = now(clock).minusMinutes(maxNrOfMinutes);
        log.info("cleanUpRecentlyAddedKlimaats before {}", cleanUpAllBefore);
        recentlyAddedKlimaatsPerKlimaatSensorCode.values()
                .forEach(klimaats -> klimaats.removeIf(klimaat -> klimaat.getDatumtijd().isBefore(cleanUpAllBefore)));
    }

    private List<BigDecimal> getValidHumidities(final List<Klimaat> klimaatList) {
        return klimaatList.stream()
                          .map(Klimaat::getLuchtvochtigheid)
                          .filter(this::isValidKlimaatValue)
                          .toList();
    }

    private List<BigDecimal> getValidTemperatures(final List<Klimaat> klimaatList) {
        return klimaatList.stream()
                          .map(Klimaat::getTemperatuur)
                          .filter(this::isValidKlimaatValue)
                          .toList();
    }

    private boolean isValidKlimaatValue(@Nullable final BigDecimal value) {
        return value != null && value.compareTo(ZERO) != 0;
    }

    public RealtimeKlimaat getMostRecent(final String klimaatSensorCode) {
        final List<Klimaat> recentlyReceivedKlimaatForSensor =
                recentlyAddedKlimaatsPerKlimaatSensorCode.getOrDefault(klimaatSensorCode, new ArrayList<>());

        return getMostRecent(recentlyReceivedKlimaatForSensor)
                .map(this::mapToRealtimeKlimaat)
                .orElse(null);
    }

    private Optional<Klimaat> getMostRecent(final List<Klimaat> klimaats) {
        return klimaats.stream().max(comparing(Klimaat::getDatumtijd));
    }

    private void publishEvent(final Klimaat klimaat) {
        final RealtimeKlimaat realtimeKlimaat = mapToRealtimeKlimaat(klimaat);
        messagingTemplate.convertAndSend(REALTIME_KLIMAAT_TOPIC, realtimeKlimaat);
    }

    private RealtimeKlimaat mapToRealtimeKlimaat(final Klimaat klimaat) {
        final RealtimeKlimaat.RealtimeKlimaatBuilder realtimeKlimaatBuilder = aRealtimeKlimaat()
            .datumtijd(klimaat.getDatumtijd())
            .luchtvochtigheid(klimaat.getLuchtvochtigheid())
            .temperatuur(klimaat.getTemperatuur())
            .sensorCode(klimaat.getKlimaatSensor().getCode());

        final List<Klimaat> klimaatsToDetermineTrendFor = getKlimaatsAddedInLastNumberOfMinutes(
                klimaat.getKlimaatSensor().getCode(), NR_OF_MINUTES_TO_DETERMINE_TREND_FOR);

        final Trend temperatureTrend = klimaatSensorValueTrendService
                .determineValueTrend(klimaatsToDetermineTrendFor, Klimaat::getTemperatuur);
        realtimeKlimaatBuilder.temperatuurTrend(temperatureTrend);

        final Trend humidityTrend = klimaatSensorValueTrendService
                .determineValueTrend(klimaatsToDetermineTrendFor, Klimaat::getLuchtvochtigheid);
        realtimeKlimaatBuilder.luchtvochtigheidTrend(humidityTrend);

        return realtimeKlimaatBuilder.build();
    }
}
