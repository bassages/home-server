package nl.homeserver.klimaat;

import static java.time.Month.FEBRUARY;
import static java.util.Collections.emptyList;
import static nl.homeserver.klimaat.KlimaatSensorBuilder.aKlimaatSensor;
import static nl.homeserver.klimaat.RealtimeKlimaatBuilder.aRealtimeKlimaat;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UNKNOWN;
import static org.springframework.boot.actuate.health.Status.UP;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
    public void whenNoMeterstandExistsThenHealthIsUnknown() {
        timeTravelTo(clock, LocalDate.of(2017, FEBRUARY, 5).atStartOfDay());

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode("LivingRoom").build();

        when(klimaatService.getAllKlimaatSensors()).thenReturn(List.of(klimaatSensor));
        when(klimaatService.getMostRecent(klimaatSensor.getCode())).thenReturn(null);

        final Health health = klimaatSensorHealth.health();

        assertThat(health.getDetails()).containsEntry("message", "No Klimaat registered yet.");
        assertThat(health.getStatus()).isEqualTo(UNKNOWN);
    }

    @Test
    public void givenNoSensorsExistThenHealthIsUnknown() {
        when(klimaatService.getAllKlimaatSensors()).thenReturn(emptyList());

        final Health health = klimaatSensorHealth.health();
        assertThat(health.getDetails()).containsEntry("message", "No KlimaatSensors found.");
        assertThat(health.getStatus()).isEqualTo(UNKNOWN);
    }

    @Test
    public void whenMostRecentMeterstandIsTenMinutesOldThenHealthIsUp() {
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode("LivingRoom").build();
        when(klimaatService.getAllKlimaatSensors()).thenReturn(List.of(klimaatSensor));

        final RealtimeKlimaat klimaatThatIsTenMinutesOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10)).build();
        when(klimaatService.getMostRecent(klimaatSensor.getCode())).thenReturn(klimaatThatIsTenMinutesOld);

        final Health health = klimaatSensorHealth.health();

        assertThat(health.getDetails()).containsEntry("message", "LivingRoom (UP) - Most recent valid klimaat was saved at 2017-02-05T09:55:00.");
        assertThat(health.getStatus()).isEqualTo(UP);
    }

    @Test
    public void whenMostRecentMeterstandIsMoreThenTenMinutesOldThenHealthIsDown() {
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode("LivingRoom").build();
        when(klimaatService.getAllKlimaatSensors()).thenReturn(List.of(klimaatSensor));

        final RealtimeKlimaat klimaatThatIsMoreThanTenMinutesOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10).minusSeconds(1)).build();
        when(klimaatService.getMostRecent(klimaatSensor.getCode())).thenReturn(klimaatThatIsMoreThanTenMinutesOld);

        final Health health = klimaatSensorHealth.health();

        assertThat(health.getDetails()).containsEntry("message", "LivingRoom (DOWN) - Most recent valid klimaat was saved at 2017-02-05T09:54:59. Which is more than 10 minutes ago.");
        assertThat(health.getStatus()).isEqualTo(DOWN);
    }

    @Test
    public void whenOneOfMultipleSensorsIsDownThenHealthIsDown() {
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final KlimaatSensor klimaatSensorLivingRoom = aKlimaatSensor().withCode("LivingRoom").build();
        final RealtimeKlimaat klimaatThatIsTooOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10).minusSeconds(1)).build();
        when(klimaatService.getMostRecent(klimaatSensorLivingRoom.getCode())).thenReturn(klimaatThatIsTooOld);

        final KlimaatSensor klimaatSensorGarden = aKlimaatSensor().withCode("Garden").build();
        final RealtimeKlimaat klimaatThatIsVeryRecent = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusSeconds(10)).build();
        when(klimaatService.getMostRecent(klimaatSensorGarden.getCode())).thenReturn(klimaatThatIsVeryRecent);

        when(klimaatService.getAllKlimaatSensors()).thenReturn(List.of(klimaatSensorLivingRoom, klimaatSensorGarden));

        final Health health = klimaatSensorHealth.health();

        final String message = health.getDetails().get("message").toString();
        assertThat(message)
                .contains("LivingRoom (DOWN) - Most recent valid klimaat was saved at 2017-02-05T09:54:59. Which is more than 10 minutes ago.")
                .contains("Garden (UP) - Most recent valid klimaat was saved at 2017-02-05T10:04:50.");
        assertThat(health.getStatus()).isEqualTo(DOWN);
    }

    @Test
    public void whenAllOfMultipleSensorsAreUpThenHealthIsUp() {
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final KlimaatSensor klimaatSensorLivingRoom = aKlimaatSensor().withCode("LivingRoom").build();
        final RealtimeKlimaat klimaatThatIsTooOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusSeconds(10)).build();
        when(klimaatService.getMostRecent(klimaatSensorLivingRoom.getCode())).thenReturn(klimaatThatIsTooOld);

        final KlimaatSensor klimaatSensorGarden = aKlimaatSensor().withCode("Garden").build();
        final RealtimeKlimaat klimaatThatIsVeryRecent = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusSeconds(10)).build();
        when(klimaatService.getMostRecent(klimaatSensorGarden.getCode())).thenReturn(klimaatThatIsVeryRecent);

        when(klimaatService.getAllKlimaatSensors()).thenReturn(List.of(klimaatSensorLivingRoom, klimaatSensorGarden));

        final Health health = klimaatSensorHealth.health();

        final String message = health.getDetails().get("message").toString();
        assertThat(message)
                .contains("LivingRoom (UP) - Most recent valid klimaat was saved at 2017-02-05T10:04:50.")
                .contains("Garden (UP) - Most recent valid klimaat was saved at 2017-02-05T10:04:50.");
        assertThat(health.getStatus()).isEqualTo(UP);
    }
}