package nl.homeserver.energie;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;

import java.time.Clock;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SlimmeMeterHealth implements HealthIndicator {

    private static final int MAXIMUM_MESSAGE_AGE_IN_MINUTES = 5;

    private static final String DETAIL_KEY_MESSAGE = "message";

    private final MeterstandService meterstandService;
    private final Clock clock;

    public SlimmeMeterHealth(final MeterstandService meterstandService,
                             final Clock clock) {
        this.meterstandService = meterstandService;
        this.clock = clock;
    }

    @Override
    public Health health() {
        final Meterstand mostRecent = meterstandService.getMostRecent();

        if (mostRecent == null) {
            return Health.unknown()
                         .withDetail(DETAIL_KEY_MESSAGE, "No Meterstand registered yet")
                         .build();

        } else if (mostRecent.getDateTime().isBefore(now(clock).minusMinutes(MAXIMUM_MESSAGE_AGE_IN_MINUTES))) {
            return Health.down()
                         .withDetail(DETAIL_KEY_MESSAGE, format("Most recent valid Meterstand was saved at %s. Which is more than %d minutes ago.", formatDatumtjd(mostRecent), MAXIMUM_MESSAGE_AGE_IN_MINUTES))
                         .build();

        } else {
            return Health.up()
                         .withDetail(DETAIL_KEY_MESSAGE, format("Most recent valid Meterstand was saved at %s", formatDatumtjd(mostRecent)))
                         .build();
        }
    }

    private String formatDatumtjd(final Meterstand mostRecent) {
        return mostRecent.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
