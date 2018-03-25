package nl.homeserver.klimaat;

import static java.time.Month.FEBRUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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

        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode("LivingRoom");

        when(klimaatService.getAllKlimaatSensors()).thenReturn(singletonList(klimaatSensor));
        when(klimaatService.getMostRecent(klimaatSensor.getCode())).thenReturn(null);

        Health health = klimaatSensorHealth.health();

        assertThat(health.getDetails().get("message")).isEqualTo("No Klimaat registered yet.");
        assertThat(health.getStatus()).isEqualTo(UNKNOWN);
    }

    @Test
    public void givenNoSensorsExistThenHealthIsUnknown() {
        when(klimaatService.getAllKlimaatSensors()).thenReturn(emptyList());

        Health health = klimaatSensorHealth.health();
        assertThat(health.getDetails().get("message")).isEqualTo("No KlimaatSensors found.");
        assertThat(health.getStatus()).isEqualTo(UNKNOWN);
    }

    @Test
    public void whenMostRecentMeterstandIsTenMinutesOldThenHealthIsUp() {
        LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode("LivingRoom");

        when(klimaatService.getAllKlimaatSensors()).thenReturn(singletonList(klimaatSensor));

        RealtimeKlimaat klimaatThatIsTenMinutesOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10)).build();
        when(klimaatService.getMostRecent(klimaatSensor.getCode())).thenReturn(klimaatThatIsTenMinutesOld);

        Health health = klimaatSensorHealth.health();

        assertThat(health.getDetails().get("message")).isEqualTo("LivingRoom (UP) - Most recent valid klimaat was saved at 2017-02-05T09:55:00.");
        assertThat(health.getStatus()).isEqualTo(UP);
    }

    @Test
    public void whenMostRecentMeterstandIsMoreThenTenMinutesOldThenHealthIsDown() {
        LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode("LivingRoom");

        when(klimaatService.getAllKlimaatSensors()).thenReturn(singletonList(klimaatSensor));

        RealtimeKlimaat klimaatThatIsMoreThanTenMinutesOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10).minusSeconds(1)).build();
        when(klimaatService.getMostRecent(klimaatSensor.getCode())).thenReturn(klimaatThatIsMoreThanTenMinutesOld);

        Health health = klimaatSensorHealth.health();

        assertThat(health.getDetails().get("message")).isEqualTo("LivingRoom (DOWN) - Most recent valid klimaat was saved at 2017-02-05T09:54:59. Which is more than 10 minutes ago.");
        assertThat(health.getStatus()).isEqualTo(DOWN);
    }

    @Test
    public void whenOneOfMultipleSensorsIsDownThenHealthIsDown() {
        LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        KlimaatSensor klimaatSensorLivingRoom = new KlimaatSensor();
        klimaatSensorLivingRoom.setCode("LivingRoom");
        RealtimeKlimaat klimaatThatIsTooOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusMinutes(10).minusSeconds(1)).build();
        when(klimaatService.getMostRecent(klimaatSensorLivingRoom.getCode())).thenReturn(klimaatThatIsTooOld);

        KlimaatSensor klimaatSensorGarden = new KlimaatSensor();
        klimaatSensorGarden.setCode("Garden");
        RealtimeKlimaat klimaatThatIsVeryRecent = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusSeconds(10)).build();
        when(klimaatService.getMostRecent(klimaatSensorGarden.getCode())).thenReturn(klimaatThatIsVeryRecent);

        when(klimaatService.getAllKlimaatSensors()).thenReturn(asList(klimaatSensorLivingRoom, klimaatSensorGarden));

        Health health = klimaatSensorHealth.health();

        String message = health.getDetails().get("message").toString();
        assertThat(message).contains("LivingRoom (DOWN) - Most recent valid klimaat was saved at 2017-02-05T09:54:59. Which is more than 10 minutes ago.");
        assertThat(message).contains("Garden (UP) - Most recent valid klimaat was saved at 2017-02-05T10:04:50.");
        assertThat(health.getStatus()).isEqualTo(DOWN);
    }

    @Test
    public void whenAllOfMultipleSensorsAreUpThenHealthIsUp() {
        LocalDateTime fixedLocalDateTime = LocalDate.of(2017, FEBRUARY, 5).atTime(10, 5);
        timeTravelTo(clock, fixedLocalDateTime);

        KlimaatSensor klimaatSensorLivingRoom = new KlimaatSensor();
        klimaatSensorLivingRoom.setCode("LivingRoom");
        RealtimeKlimaat klimaatThatIsTooOld = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusSeconds(10)).build();
        when(klimaatService.getMostRecent(klimaatSensorLivingRoom.getCode())).thenReturn(klimaatThatIsTooOld);

        KlimaatSensor klimaatSensorGarden = new KlimaatSensor();
        klimaatSensorGarden.setCode("Garden");
        RealtimeKlimaat klimaatThatIsVeryRecent = aRealtimeKlimaat().withDatumtijd(fixedLocalDateTime.minusSeconds(10)).build();
        when(klimaatService.getMostRecent(klimaatSensorGarden.getCode())).thenReturn(klimaatThatIsVeryRecent);

        when(klimaatService.getAllKlimaatSensors()).thenReturn(asList(klimaatSensorLivingRoom, klimaatSensorGarden));

        Health health = klimaatSensorHealth.health();

        String message = health.getDetails().get("message").toString();
        assertThat(message).contains("LivingRoom (UP) - Most recent valid klimaat was saved at 2017-02-05T10:04:50.");
        assertThat(message).contains("Garden (UP) - Most recent valid klimaat was saved at 2017-02-05T10:04:50.");
        assertThat(health.getStatus()).isEqualTo(UP);
    }
}