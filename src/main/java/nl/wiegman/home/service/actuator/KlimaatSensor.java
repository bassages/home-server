package nl.wiegman.home.service.actuator;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.service.KlimaatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class KlimaatSensor implements HealthIndicator {

    private static final int MAXIMUM_KLIMAAT_AGE_IN_MINUTES = 20;

    @Autowired
    KlimaatService klimaatService;

    @Override
    public Health health() {

        Date now = new Date();

        Klimaat mostRecentKlimaat = klimaatService.getMostRecent();

        String formattedKlimaatDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(mostRecentKlimaat.getDatumtijd());

        if (mostRecentKlimaat.getDatumtijd().getTime() < (now.getTime() - TimeUnit.MINUTES.toMillis(MAXIMUM_KLIMAAT_AGE_IN_MINUTES))) {
            return Health.down()
                    .withDetail("message", "Most recent valid klimaat was saved at " + formattedKlimaatDateTime + ". Which is more than " + MAXIMUM_KLIMAAT_AGE_IN_MINUTES + " minutes ago.")
                    .build();
        } else {
            return Health.up()
                    .withDetail("message", "Most recent valid klimaat was saved at " + formattedKlimaatDateTime)
                    .build();
        }
    }

}
