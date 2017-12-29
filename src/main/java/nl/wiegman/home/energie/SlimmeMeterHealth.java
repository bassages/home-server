package nl.wiegman.home.energie;

import static java.lang.String.format;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SlimmeMeterHealth implements HealthIndicator {

    private static final int MAXIMUM_MESSAGE_AGE_IN_MINUTES = 5;

    private static final String DETAIL_KEY_MESSAGE = "message";

    private final MeterstandService meterstandService;
    private final Clock clock;

    @Autowired
    public SlimmeMeterHealth(MeterstandService meterstandService, Clock clock) {
        this.meterstandService = meterstandService;
        this.clock = clock;
    }

    @Override
    public Health health() {
        Meterstand mostRecent = meterstandService.getMostRecent();

        if (mostRecent == null) {
            return Health.unknown()
                         .withDetail(DETAIL_KEY_MESSAGE, "No Meterstand received since application startup")
                         .build();

        } else if (mostRecent.getDatumtijdAsLocalDateTime().isBefore(LocalDateTime.now(clock).minusMinutes(MAXIMUM_MESSAGE_AGE_IN_MINUTES))) {
            return Health.down()
                         .withDetail(DETAIL_KEY_MESSAGE, format("Most recent valid Meterstand was saved at %s. Which is more than %d minutes ago.", formatDatumtjd(mostRecent), MAXIMUM_MESSAGE_AGE_IN_MINUTES))
                         .build();

        } else {
            return Health.up()
                         .withDetail(DETAIL_KEY_MESSAGE, format("Most recent valid Meterstand was saved at %s", formatDatumtjd(mostRecent)))
                         .build();
        }
    }

    private String formatDatumtjd(Meterstand mostRecent) {
        return mostRecent.getDatumtijdAsLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
