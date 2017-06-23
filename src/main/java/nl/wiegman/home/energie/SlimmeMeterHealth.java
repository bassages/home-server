package nl.wiegman.home.energie;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SlimmeMeterHealth implements HealthIndicator {

    private static final int MAXIMUM_MESSAGE_AGE_IN_MINUTES = 5;

    private final MeterstandService meterstandService;

    @Autowired
    public SlimmeMeterHealth(MeterstandService meterstandService) {
        this.meterstandService = meterstandService;
    }

    @Override
    public Health health() {
        Date now = new Date();

        Meterstand mostRecent = meterstandService.getMeestRecente();

        String formattedDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(mostRecent.getDatumtijdAsDate());

        if (mostRecent.getDatumtijd() < (now.getTime() - TimeUnit.MINUTES.toMillis(MAXIMUM_MESSAGE_AGE_IN_MINUTES))) {
            return Health.down()
                    .withDetail("message", "Most recent valid Meterstand was saved at " + formattedDateTime + ". Which is more than " + MAXIMUM_MESSAGE_AGE_IN_MINUTES + " minutes ago.")
                    .build();
        } else {
            return Health.up()
                    .withDetail("message", "Most recent valid Meterstand was saved at " + formattedDateTime)
                    .build();
        }
    }
}
