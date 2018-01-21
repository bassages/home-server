package nl.homeserver.klimaat;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.unknown;
import static org.springframework.boot.actuate.health.Health.up;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class KlimaatSensorHealth implements HealthIndicator {

    private static final int MAXIMUM_KLIMAAT_AGE_IN_MINUTES = 10;

    private static final String DETAIL_KEY_MESSAGE = "message";

    private final KlimaatService klimaatService;
    private final Clock clock;

    public KlimaatSensorHealth(KlimaatService klimaatService, Clock clock) {
        this.klimaatService = klimaatService;
        this.clock = clock;
    }

    @Override
    public Health health() {
        List<KlimaatSensor> allKlimaatSensors = klimaatService.getAllKlimaatSensors();

        if (isEmpty(allKlimaatSensors)) {
            return unknown().withDetail(DETAIL_KEY_MESSAGE, "No KlimaatSensors found.").build();
        }

        Map<KlimaatSensor, RealtimeKlimaat> mostRecentlyRegisteredKlimaatPerSensor = getMostRecentlyRegisteredKlimaatPerSensor(allKlimaatSensors);

        if (noSensorHasRegisteredKlimaat(mostRecentlyRegisteredKlimaatPerSensor)) {
            return unknown().withDetail(DETAIL_KEY_MESSAGE, "No Klimaat registered yet.").build();
        } else if (atLeastOneIsDown(mostRecentlyRegisteredKlimaatPerSensor)) {
            return down().withDetail(DETAIL_KEY_MESSAGE, createMessage(mostRecentlyRegisteredKlimaatPerSensor)).build();
        } else {
            return up().withDetail(DETAIL_KEY_MESSAGE, createMessage(mostRecentlyRegisteredKlimaatPerSensor)).build();
        }
    }

    private boolean noSensorHasRegisteredKlimaat(Map<KlimaatSensor, RealtimeKlimaat> mostRecentlyRegisteredKlimaatPerSensor) {
        return mostRecentlyRegisteredKlimaatPerSensor.values().stream().allMatch(Objects::isNull);
    }

    private String createMessage(Map<KlimaatSensor, RealtimeKlimaat> mostRecentForAllSensors) {
        return mostRecentForAllSensors.entrySet().stream()
                                                 .map(this::createMessage)
                                                 .collect(Collectors.joining("\n"));
    }

    private String createMessage(Map.Entry<KlimaatSensor, RealtimeKlimaat> klimaatSensorRealtimeKlimaatEntry) {
        KlimaatSensor klimaatSensor = klimaatSensorRealtimeKlimaatEntry.getKey();
        RealtimeKlimaat mostRecentlyReceivedKlimaatForSensor = klimaatSensorRealtimeKlimaatEntry.getValue();

        if (isDown(mostRecentlyReceivedKlimaatForSensor)) {
            return format("%s (%s) - Most recent valid klimaat was saved at %s. Which is more than %d minutes ago.", klimaatSensor.getCode(), DOWN.toString(), formatDatumtijd(mostRecentlyReceivedKlimaatForSensor), MAXIMUM_KLIMAAT_AGE_IN_MINUTES);
        } else {
            return format("%s (%s) - Most recent valid klimaat was saved at %s.", klimaatSensor.getCode(), UP.toString(), formatDatumtijd(mostRecentlyReceivedKlimaatForSensor));
        }
    }

    private boolean atLeastOneIsDown(Map<KlimaatSensor, RealtimeKlimaat> mostRecentForAllSensors) {
        return mostRecentForAllSensors.values().stream().anyMatch(this::isDown);
    }

    private boolean isDown(RealtimeKlimaat mostRecentRealtimeKlimaat) {
        return mostRecentRealtimeKlimaat.getDatumtijd().isBefore(now(clock).minusMinutes(MAXIMUM_KLIMAAT_AGE_IN_MINUTES));
    }

    private Map<KlimaatSensor, RealtimeKlimaat> getMostRecentlyRegisteredKlimaatPerSensor(List<KlimaatSensor> allKlimaatSensors) {
        Map<KlimaatSensor, RealtimeKlimaat> mostRecentlyRegisteredKlimaatPerSensor = new HashMap<>();

        for (KlimaatSensor klimaatSensor : allKlimaatSensors) {
            RealtimeKlimaat mostRecent = klimaatService.getMostRecent(klimaatSensor.getCode());
            mostRecentlyRegisteredKlimaatPerSensor.put(klimaatSensor, mostRecent);
        }
        return mostRecentlyRegisteredKlimaatPerSensor;
    }

    private String formatDatumtijd(RealtimeKlimaat mostRecent) {
        return mostRecent.getDatumtijd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
