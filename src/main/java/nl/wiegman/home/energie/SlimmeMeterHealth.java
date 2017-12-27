package nl.wiegman.home.energie;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SlimmeMeterHealth implements HealthIndicator {

    private static final int MAXIMUM_MESSAGE_AGE_IN_MINUTES = 5;

    private static final String DETAIL_KEY_MESSAGE = "message";

    private final MeterstandService meterstandService;

    @Autowired
    public SlimmeMeterHealth(MeterstandService meterstandService) {
        this.meterstandService = meterstandService;
    }

    @Override
    public Health health() {
        Date now = new Date();

        Meterstand mostRecent = meterstandService.getMostRecent();

        if (mostRecent == null) {

            return Health.unknown()
                         .withDetail(DETAIL_KEY_MESSAGE, "No Meterstand received since application startup")
                         .build();

        } else if (mostRecent.getDatumtijd() < (now.getTime() - TimeUnit.MINUTES.toMillis(MAXIMUM_MESSAGE_AGE_IN_MINUTES))) {

            return Health.down()
                         .withDetail(
                                 DETAIL_KEY_MESSAGE, "Most recent valid Meterstand was saved at " + formatDatumtjd(mostRecent.getDatumtijdAsLocalDateTime()) + ". Which is more than " + MAXIMUM_MESSAGE_AGE_IN_MINUTES + " minutes ago.")
                         .build();

        } else {

            return Health.up()
                         .withDetail(DETAIL_KEY_MESSAGE, "Most recent valid Meterstand was saved at " + formatDatumtjd(mostRecent.getDatumtijdAsLocalDateTime()))
                         .build();
        }
    }

    private String formatDatumtjd(LocalDateTime datumtijd) {
        return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(datumtijd);
    }
}
