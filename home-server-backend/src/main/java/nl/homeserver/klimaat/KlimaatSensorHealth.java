package nl.homeserver.klimaat;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.boot.actuate.health.Health.*;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;

@Component
public class KlimaatSensorHealth implements HealthIndicator {

    private static final int MAXIMUM_KLIMAAT_AGE_IN_MINUTES = 10;

    private static final String DETAIL_KEY_MESSAGE = "message";

    private final KlimaatService klimaatService;
    private final Clock clock;

    public KlimaatSensorHealth(final KlimaatService klimaatService, final Clock clock) {
        this.klimaatService = klimaatService;
        this.clock = clock;
    }

    @Override
    public Health health() {
        final List<KlimaatSensor> allKlimaatSensors = klimaatService.getAllKlimaatSensors();

        if (isEmpty(allKlimaatSensors)) {
            return unknown().withDetail(DETAIL_KEY_MESSAGE, "No KlimaatSensors found.").build();
        }

        final Map<KlimaatSensor, RealtimeKlimaat> mostRecentlyRegisteredKlimaatPerSensor = getMostRecentlyRegisteredKlimaatPerSensor(allKlimaatSensors);

        if (noSensorHasRegisteredKlimaat(mostRecentlyRegisteredKlimaatPerSensor)) {
            return unknown().withDetail(DETAIL_KEY_MESSAGE, "No Klimaat registered yet.").build();
        } else if (atLeastOneIsDown(mostRecentlyRegisteredKlimaatPerSensor)) {
            return down().withDetail(DETAIL_KEY_MESSAGE, createMessage(mostRecentlyRegisteredKlimaatPerSensor)).build();
        } else {
            return up().withDetail(DETAIL_KEY_MESSAGE, createMessage(mostRecentlyRegisteredKlimaatPerSensor)).build();
        }
    }

    private boolean noSensorHasRegisteredKlimaat(final Map<KlimaatSensor, RealtimeKlimaat> mostRecentlyRegisteredKlimaatPerSensor) {
        return mostRecentlyRegisteredKlimaatPerSensor.values().stream().allMatch(Objects::isNull);
    }

    private String createMessage(final Map<KlimaatSensor, RealtimeKlimaat> mostRecentForAllSensors) {
        return mostRecentForAllSensors.entrySet().stream()
                                                 .map(this::createMessage)
                                                 .collect(Collectors.joining("\n"));
    }

    private String createMessage(final Map.Entry<KlimaatSensor, RealtimeKlimaat> klimaatSensorRealtimeKlimaatEntry) {
        final KlimaatSensor klimaatSensor = klimaatSensorRealtimeKlimaatEntry.getKey();
        final RealtimeKlimaat mostRecentlyReceivedKlimaatForSensor = klimaatSensorRealtimeKlimaatEntry.getValue();

        if (isDown(mostRecentlyReceivedKlimaatForSensor)) {
            return format("%s (%s) - Most recent valid klimaat was saved at %s. Which is more than %d minutes ago.",
                          klimaatSensor.getCode(), DOWN.toString(),
                          formatDatumtijd(mostRecentlyReceivedKlimaatForSensor), MAXIMUM_KLIMAAT_AGE_IN_MINUTES);
        } else {
            return format("%s (%s) - Most recent valid klimaat was saved at %s.",
                          klimaatSensor.getCode(), UP.toString(),
                          formatDatumtijd(mostRecentlyReceivedKlimaatForSensor));
        }
    }

    private boolean atLeastOneIsDown(final Map<KlimaatSensor, RealtimeKlimaat> mostRecentForAllSensors) {
        return mostRecentForAllSensors.values().stream().anyMatch(this::isDown);
    }

    private boolean isDown(final RealtimeKlimaat mostRecentRealtimeKlimaat) {
        return mostRecentRealtimeKlimaat.getDatumtijd().isBefore(now(clock).minusMinutes(MAXIMUM_KLIMAAT_AGE_IN_MINUTES));
    }

    private Map<KlimaatSensor, RealtimeKlimaat> getMostRecentlyRegisteredKlimaatPerSensor(final List<KlimaatSensor> allKlimaatSensors) {
        final Map<KlimaatSensor, RealtimeKlimaat> mostRecentlyRegisteredKlimaatPerSensor = new HashMap<>();

        for (final KlimaatSensor klimaatSensor : allKlimaatSensors) {
            final RealtimeKlimaat mostRecent = klimaatService.getMostRecent(klimaatSensor.getCode());
            mostRecentlyRegisteredKlimaatPerSensor.put(klimaatSensor, mostRecent);
        }
        return mostRecentlyRegisteredKlimaatPerSensor;
    }

    private String formatDatumtijd(final RealtimeKlimaat mostRecent) {
        return mostRecent.getDatumtijd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
