package nl.homeserver.energie.meterstand;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.Month.FEBRUARY;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Status.*;

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
        when(meterstandService.getMostRecent()).thenReturn(null);

        final Health health = meterstandHealth.health();

        assertThat(health.getDetails()).containsEntry("message", "No Meterstand registered yet");
        assertThat(health.getStatus()).isEqualTo(UNKNOWN);
    }

    @Test
    void givenMostRecentMeterstandIsFiveMinutesOldWhenGetHealthThenHealthIsUp() {
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final Meterstand meterstandThatIsFiveMinutesOld = MeterstandBuilder.aMeterstand().withDateTime(fixedLocalDateTime.minusMinutes(5)).build();
        when(meterstandService.getMostRecent()).thenReturn(meterstandThatIsFiveMinutesOld);

        final Health health = meterstandHealth.health();

        assertThat(health.getDetails()).containsEntry("message", "Most recent valid Meterstand was saved at 2017-02-05T10:00:00");
        assertThat(health.getStatus()).isEqualTo(UP);
    }

    @Test
    void givenMostRecentMeterstandIsMoreThenFiveMinutesOldWhenGetHealthThenHealthIsDown() {
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final Meterstand meterstandThatIsFiveMinutesOld = MeterstandBuilder.aMeterstand().withDateTime(fixedLocalDateTime.minusMinutes(5).minusSeconds(1)).build();
        when(meterstandService.getMostRecent()).thenReturn(meterstandThatIsFiveMinutesOld);

        final Health health = meterstandHealth.health();

        assertThat(health.getDetails()).containsEntry("message", "Most recent valid Meterstand was saved at 2017-02-05T09:59:59. Which is more than 5 minutes ago.");
        assertThat(health.getStatus()).isEqualTo(DOWN);
    }
}
