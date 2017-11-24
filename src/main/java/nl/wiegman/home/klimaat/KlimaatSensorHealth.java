package nl.wiegman.home.klimaat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class KlimaatSensorHealth implements HealthIndicator {

    private static final int MAXIMUM_KLIMAAT_AGE_IN_MINUTES = 10;

    private static final String DETAIL_KEY_OF_MESSAGE = "message";

    private final KlimaatService klimaatService;

    @Autowired
    public KlimaatSensorHealth(KlimaatService klimaatService) {
        this.klimaatService = klimaatService;
    }

    @Override
    public Health health() {

        Date now = new Date();

        RealtimeKlimaat mostRecent = klimaatService.getMostRecent(KlimaatController.DEFAULT_KLIMAAT_SENSOR_CODE);

        if (mostRecent == null) {
            return Health.unknown()
                    .withDetail(DETAIL_KEY_OF_MESSAGE, "No valid klimaat received since application startup")
                    .build();
        } else if (mostRecent.getDatumtijd().getTime() < (now.getTime() - TimeUnit.MINUTES.toMillis(MAXIMUM_KLIMAAT_AGE_IN_MINUTES))) {
            return Health.down()
                    .withDetail(DETAIL_KEY_OF_MESSAGE, "Most recent valid klimaat was saved at " + formatDatumtijd(mostRecent) + ". Which is more than " + MAXIMUM_KLIMAAT_AGE_IN_MINUTES + " minutes ago.")
                    .build();
        } else {
            return Health.up()
                    .withDetail(DETAIL_KEY_OF_MESSAGE, "Most recent valid klimaat was saved at " + formatDatumtijd(mostRecent))
                    .build();
        }
    }

    private String formatDatumtijd(RealtimeKlimaat mostRecent) {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(mostRecent.getDatumtijd());
    }

}
