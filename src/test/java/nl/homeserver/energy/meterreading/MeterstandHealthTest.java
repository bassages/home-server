package nl.homeserver.energy.meterreading;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.Month.FEBRUARY;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterstandHealthTest {

    @InjectMocks
    MeterstandHealth meterstandHealth;

    @Mock
    MeterstandService meterstandService;
    @Mock
    Clock clock;

    @Test
    void givenNoMeterstandExistsWhenGetHealthThenHealthIsUnknown() {
        when(meterstandService.getMostRecent()).thenReturn(Optional.empty());

        final Health health = meterstandHealth.health();

        assertThat(health.getDetails()).containsEntry("message", "No Meterstand registered yet");
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
    }

    @Test
    void givenMostRecentMeterstandIsFiveMinutesOldWhenGetHealthThenHealthIsUp() {
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final Meterstand meterstandThatIsFiveMinutesOld = MeterstandBuilder.aMeterstand().withDateTime(fixedLocalDateTime.minusMinutes(5)).build();
        when(meterstandService.getMostRecent()).thenReturn(Optional.of(meterstandThatIsFiveMinutesOld));

        final Health health = meterstandHealth.health();

        assertThat(health.getDetails()).containsEntry("message", "Most recent valid Meterstand was saved at 2017-02-05T10:00:00");
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void givenMostRecentMeterstandIsMoreThenFiveMinutesOldWhenGetHealthThenHealthIsDown() {
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final Meterstand meterstandThatIsFiveMinutesOld = MeterstandBuilder.aMeterstand().withDateTime(fixedLocalDateTime.minusMinutes(5).minusSeconds(1)).build();
        when(meterstandService.getMostRecent()).thenReturn(Optional.of(meterstandThatIsFiveMinutesOld));

        final Health health = meterstandHealth.health();

        assertThat(health.getDetails()).containsEntry("message", "Most recent valid Meterstand was saved at 2017-02-05T09:59:59. Which is more than 5 minutes ago.");
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
