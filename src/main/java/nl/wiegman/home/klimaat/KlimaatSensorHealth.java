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

    private static final int MAXIMUM_KLIMAAT_AGE_IN_MINUTES = 20;

    private final KlimaatService klimaatService;

    @Autowired
    public KlimaatSensorHealth(KlimaatService klimaatService) {
        this.klimaatService = klimaatService;
    }

    @Override
    public Health health() {

        Date now = new Date();

        Klimaat mostRecent = klimaatService.getMostRecent();

        String formattedDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(mostRecent.getDatumtijd());

        if (mostRecent.getDatumtijd().getTime() < (now.getTime() - TimeUnit.MINUTES.toMillis(MAXIMUM_KLIMAAT_AGE_IN_MINUTES))) {
            return Health.down()
                    .withDetail("message", "Most recent valid klimaat was saved at " + formattedDateTime + ". Which is more than " + MAXIMUM_KLIMAAT_AGE_IN_MINUTES + " minutes ago.")
                    .build();
        } else {
            return Health.up()
                    .withDetail("message", "Most recent valid klimaat was saved at " + formattedDateTime)
                    .build();
        }
    }

}
