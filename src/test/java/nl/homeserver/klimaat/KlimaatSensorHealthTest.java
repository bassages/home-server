package nl.homeserver.klimaat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.FEBRUARY;
import static java.util.Collections.emptyList;
import static nl.homeserver.klimaat.KlimaatSensorBuilder.aKlimaatSensor;
import static nl.homeserver.klimaat.RealtimeKlimaatBuilder.aRealtimeKlimaat;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Status.*;

@RunWith(MockitoJUnitRunner.class)
public class KlimaatSensorHealthTest {

    @InjectMocks
    private KlimaatSensorHealth klimaatSensorHealth;

    @Mock
    private KlimaatSensorService klimaatSensorService;
    @Mock
    private IncomingKlimaatService incomingKlimaatService;
    @Mock
    private Clock clock;

    @Test
    public void whenNoKlimaatExistsThenHealthIsUnknown() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, FEBRUARY, 5).atStartOfDay());

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode("LivingRoom").build();

        when(klimaatSensorService.getAll()).thenReturn(List.of(klimaatSensor));
        when(incomingKlimaatService.getMostRecent(klimaatSensor.getCode())).thenReturn(null);

        // when
        final Health health = klimaatSensorHealth.health();

        // then
        assertThat(health.getDetails()).containsEntry("message", "No Klimaat registered yet.");
        assertThat(health.getStatus()).isEqualTo(UNKNOWN);
    }

    @Test
    public void givenNoSensorsExistThenHealthIsUnknown() {
        // given
        when(klimaatSensorService.getAll()).thenReturn(emptyList());

        // when
        final Health health = klimaatSensorHealth.health();

        // then
        assertThat(health.getDetails()).containsEntry("message", "No KlimaatSensors found.");
        assertThat(health.getStatus()).isEqualTo(UNKNOWN);
    }

    @Test
    public void whenMostRecentMeterstandIsTenMinutesOldThenHealthIsUp() {
        // given
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode("LivingRoom").build();
        when(klimaatSensorService.getAll()).thenReturn(List.of(klimaatSensor));

        final RealtimeKlimaat klimaatThatIsTenMinutesOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10)).build();
        when(incomingKlimaatService.getMostRecent(klimaatSensor.getCode())).thenReturn(klimaatThatIsTenMinutesOld);

        // when
        final Health health = klimaatSensorHealth.health();

        // then
        assertThat(health.getDetails()).containsEntry("message", "LivingRoom (UP) - Most recent valid klimaat was saved at 2017-02-05T09:55:00.");
        assertThat(health.getStatus()).isEqualTo(UP);
    }

    @Test
    public void whenMostRecentMeterstandIsMoreThenTenMinutesOldThenHealthIsDown() {
        // given
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode("LivingRoom").build();
        when(klimaatSensorService.getAll()).thenReturn(List.of(klimaatSensor));

        final RealtimeKlimaat klimaatThatIsMoreThanTenMinutesOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10).minusSeconds(1)).build();
        when(incomingKlimaatService.getMostRecent(klimaatSensor.getCode())).thenReturn(klimaatThatIsMoreThanTenMinutesOld);

        // when
        final Health health = klimaatSensorHealth.health();

        // then
        assertThat(health.getDetails()).containsEntry("message", "LivingRoom (DOWN) - Most recent valid klimaat was saved at 2017-02-05T09:54:59. Which is more than 10 minutes ago.");
        assertThat(health.getStatus()).isEqualTo(DOWN);
    }

    @Test
    public void whenOneOfMultipleSensorsIsDownThenHealthIsDown() {
        // given
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final KlimaatSensor klimaatSensorLivingRoom = aKlimaatSensor().withCode("LivingRoom").build();
        final RealtimeKlimaat klimaatThatIsTooOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10).minusSeconds(1)).build();
        when(incomingKlimaatService.getMostRecent(klimaatSensorLivingRoom.getCode())).thenReturn(klimaatThatIsTooOld);

        final KlimaatSensor klimaatSensorGarden = aKlimaatSensor().withCode("Garden").build();
        final RealtimeKlimaat klimaatThatIsVeryRecent = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusSeconds(10)).build();
        when(incomingKlimaatService.getMostRecent(klimaatSensorGarden.getCode())).thenReturn(klimaatThatIsVeryRecent);

        when(klimaatSensorService.getAll()).thenReturn(List.of(klimaatSensorLivingRoom, klimaatSensorGarden));

        // when
        final Health health = klimaatSensorHealth.health();

        // then
        final String message = health.getDetails().get("message").toString();
        assertThat(message)
                .contains("LivingRoom (DOWN) - Most recent valid klimaat was saved at 2017-02-05T09:54:59. Which is more than 10 minutes ago.")
                .contains("Garden (UP) - Most recent valid klimaat was saved at 2017-02-05T10:04:50.");
        assertThat(health.getStatus()).isEqualTo(DOWN);
    }

    @Test
    public void whenAllOfMultipleSensorsAreUpThenHealthIsUp() {
        // given
        final LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        final KlimaatSensor klimaatSensorLivingRoom = aKlimaatSensor().withCode("LivingRoom").build();
        final RealtimeKlimaat klimaatThatIsTooOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusSeconds(10)).build();
        when(incomingKlimaatService.getMostRecent(klimaatSensorLivingRoom.getCode())).thenReturn(klimaatThatIsTooOld);

        final KlimaatSensor klimaatSensorGarden = aKlimaatSensor().withCode("Garden").build();
        final RealtimeKlimaat klimaatThatIsVeryRecent = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusSeconds(10)).build();
        when(incomingKlimaatService.getMostRecent(klimaatSensorGarden.getCode())).thenReturn(klimaatThatIsVeryRecent);

        when(klimaatSensorService.getAll()).thenReturn(List.of(klimaatSensorLivingRoom, klimaatSensorGarden));

        // when
        final Health health = klimaatSensorHealth.health();

        // then
        final String message = health.getDetails().get("message").toString();
        assertThat(message)
                .contains("LivingRoom (UP) - Most recent valid klimaat was saved at 2017-02-05T10:04:50.")
                .contains("Garden (UP) - Most recent valid klimaat was saved at 2017-02-05T10:04:50.");
        assertThat(health.getStatus()).isEqualTo(UP);
    }
}
