package nl.wiegman.home.klimaat;

import static java.lang.String.format;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class KlimaatSensorHealth implements HealthIndicator {

    private static final int MAXIMUM_KLIMAAT_AGE_IN_MINUTES = 10;

    private static final String DETAIL_KEY_OF_MESSAGE = "message";

    private final KlimaatService klimaatService;
    private final Clock clock;

    @Autowired
    public KlimaatSensorHealth(KlimaatService klimaatService, Clock clock) {
        this.klimaatService = klimaatService;
        this.clock = clock;
    }

    @Override
    public Health health() {
        RealtimeKlimaat mostRecent = klimaatService.getMostRecent(KlimaatController.DEFAULT_KLIMAAT_SENSOR_CODE);

        if (mostRecent == null) {
            return Health.unknown()
                         .withDetail(DETAIL_KEY_OF_MESSAGE, "No valid klimaat received since application startup")
                         .build();
        } else if (mostRecent.getDatumtijd().isBefore(LocalDateTime.now(clock).minusMinutes(MAXIMUM_KLIMAAT_AGE_IN_MINUTES))) {
            return Health.down()
                         .withDetail(DETAIL_KEY_OF_MESSAGE, format("Most recent valid klimaat was saved at %s. Which is more than %d minutes ago.", formatDatumtijd(mostRecent), MAXIMUM_KLIMAAT_AGE_IN_MINUTES))
                         .build();
        } else {
            return Health.up()
                    .withDetail(DETAIL_KEY_OF_MESSAGE, format("Most recent valid klimaat was saved at %s", formatDatumtijd(mostRecent)))
                    .build();
        }
    }

    private String formatDatumtijd(RealtimeKlimaat mostRecent) {
        return mostRecent.getDatumtijd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
