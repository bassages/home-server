package nl.homeserver.klimaat;

import static java.time.Month.FEBRUARY;
import static nl.homeserver.klimaat.RealtimeKlimaatBuilder.aRealtimeKlimaat;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UNKNOWN;
import static org.springframework.boot.actuate.health.Status.UP;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;

@RunWith(MockitoJUnitRunner.class)
public class KlimaatSensorHealthTest {

    @InjectMocks
    private KlimaatSensorHealth klimaatSensorHealth;

    @Mock
    private KlimaatService klimaatService;
    @Mock
    private Clock clock;

    @Test
    public void givenNoMeterstandExistsWhenGetHealthThenHealthIsUnknown() {
        timeTravelTo(clock, LocalDate.of(2017, FEBRUARY, 5).atTime(10, 0, 0));

        when(klimaatService.getMostRecent(any())).thenReturn(null);

        Health health = klimaatSensorHealth.health();

        assertThat(health.getDetails().get("message")).isEqualTo("No Klimaat registered yet");
        assertThat(health.getStatus()).isEqualTo(UNKNOWN);
    }

    @Test
    public void givenMostRecentMeterstandIsTenMinutesOldWhenGetHealthThenHealthIsUp() {
        LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        RealtimeKlimaat klimaatThatIsTenMinutesOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10)).build();
        when(klimaatService.getMostRecent(any())).thenReturn(klimaatThatIsTenMinutesOld);

        Health health = klimaatSensorHealth.health();

        assertThat(health.getDetails().get("message")).isEqualTo("Most recent valid klimaat was saved at 2017-02-05T09:55:00");
        assertThat(health.getStatus()).isEqualTo(UP);
    }

    @Test
    public void givenMostRecentMeterstandIsMoreThenTenMinutesOldWhenGetHealthThenHealthIsDown() {
        LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        RealtimeKlimaat klimaatThatIsMoreThanTenMinutesOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10).minusSeconds(1)).build();
        when(klimaatService.getMostRecent(any())).thenReturn(klimaatThatIsMoreThanTenMinutesOld);

        Health health = klimaatSensorHealth.health();

        assertThat(health.getDetails().get("message")).isEqualTo("Most recent valid klimaat was saved at 2017-02-05T09:54:59. Which is more than 10 minutes ago.");
        assertThat(health.getStatus()).isEqualTo(DOWN);
    }
}