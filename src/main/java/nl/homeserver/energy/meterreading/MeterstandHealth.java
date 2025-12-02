package nl.homeserver.energy.meterreading;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;

@Component
@RequiredArgsConstructor
class MeterstandHealth implements HealthIndicator {

    private static final int MAXIMUM_MESSAGE_AGE_IN_MINUTES = 5;
    private static final String DETAIL_KEY_MESSAGE = "message";

    private final MeterstandService meterstandService;
    private final Clock clock;

    @Override
    public Health health() {
        final Optional<Meterstand> optionalMostRecent = meterstandService.getMostRecent();

        return optionalMostRecent.map(mostRecent -> {
            if (mostRecent.getDateTime().isBefore(now(clock).minusMinutes(MAXIMUM_MESSAGE_AGE_IN_MINUTES))) {
                return unhealthy(mostRecent);
            } else {
                return healthy(mostRecent);
            }
        }).orElse(unknown());
    }

    private Health healthy(final Meterstand mostRecent) {
        return Health.up()
                .withDetail(DETAIL_KEY_MESSAGE,
                        format("Most recent valid Meterstand was saved at %s", formatDatumtjd(mostRecent)))
                .build();
    }

    private Health unhealthy(final Meterstand mostRecent) {
        return Health.down()
                .withDetail(DETAIL_KEY_MESSAGE,
                        format("Most recent valid Meterstand was saved at %s. Which is more than %d minutes ago.",
                                formatDatumtjd(mostRecent), MAXIMUM_MESSAGE_AGE_IN_MINUTES))
                .build();
    }

    private Health unknown() {
        return Health.unknown()
                .withDetail(DETAIL_KEY_MESSAGE, "No Meterstand registered yet")
                .build();
    }

    private String formatDatumtjd(final Meterstand mostRecent) {
        return mostRecent.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
