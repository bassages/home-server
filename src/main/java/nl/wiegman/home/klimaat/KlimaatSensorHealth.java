package nl.wiegman.home.klimaat;

import nl.wiegman.home.klimaat.Klimaat;
import nl.wiegman.home.klimaat.KlimaatSensorRepository;
import nl.wiegman.home.klimaat.KlimaatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class KlimaatSensorHealth implements HealthIndicator {

    private static final int MAXIMUM_KLIMAAT_AGE_IN_MINUTES = 20;

    private final KlimaatService klimaatService;
    private final KlimaatSensorRepository klimaatSensorRepository;

    @Autowired
    public KlimaatSensorHealth(KlimaatSensorRepository klimaatSensorRepository, KlimaatService klimaatService) {
        this.klimaatSensorRepository = klimaatSensorRepository;
        this.klimaatService = klimaatService;
    }

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
