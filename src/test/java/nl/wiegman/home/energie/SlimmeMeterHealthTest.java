package nl.wiegman.home.energie;

import static nl.wiegman.home.energie.MeterstandBuilder.aMeterstand;
import static nl.wiegman.home.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UNKNOWN;
import static org.springframework.boot.actuate.health.Status.UP;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;

@RunWith(MockitoJUnitRunner.class)
public class SlimmeMeterHealthTest {

    private static final LocalDateTime SOME_MOMENT_IN_TIME = LocalDate.of(2017, 2, 5).atTime(10, 5);

    private SlimmeMeterHealth slimmeMeterHealth;

    @Mock
    private MeterstandService meterstandService;

    public void setup(LocalDateTime localDateTimeToTimeTravelTo) {
        slimmeMeterHealth = new SlimmeMeterHealth(meterstandService, timeTravelTo(localDateTimeToTimeTravelTo));
    }

    @Test
    public void givenNoMeterstandExistsWhenGetHealthThenHealthIsUnknown() {
        setup(LocalDate.of(2017, 2, 5).atTime(10, 0, 0));

        when(meterstandService.getMostRecent()).thenReturn(null);

        Health health = slimmeMeterHealth.health();

        assertThat(health.getStatus()).isEqualTo(UNKNOWN);
        assertThat(health.getDetails().get("message")).isEqualTo("No Meterstand registered yet");
    }

    @Test
    public void givenMostRecentMeterstandIsFiveMinutesOldWhenGetHealthThenHealthIsUp() {
        setup(SOME_MOMENT_IN_TIME);

        Meterstand meterstandThatIsFiveMinutesOld = aMeterstand().withDatumTijd(SOME_MOMENT_IN_TIME.minusMinutes(5)).build();
        when(meterstandService.getMostRecent()).thenReturn(meterstandThatIsFiveMinutesOld);

        Health health = slimmeMeterHealth.health();

        assertThat(health.getStatus()).isEqualTo(UP);
        assertThat(health.getDetails().get("message")).isEqualTo("Most recent valid Meterstand was saved at 2017-02-05T10:00:00");
    }

    @Test
    public void givenMostRecentMeterstandIsMoreThenFiveMinutesOldWhenGetHealthThenHealthIsDown() {
        LocalDateTime currentDateTime = LocalDate.of(2017, 2, 5).atTime(10, 0, 0);
        setup(currentDateTime);

        Meterstand meterstandThatIsFiveMinutesOld = aMeterstand().withDatumTijd(currentDateTime.minusMinutes(5).minusSeconds(1)).build();
        when(meterstandService.getMostRecent()).thenReturn(meterstandThatIsFiveMinutesOld);

        Health health = slimmeMeterHealth.health();

        assertThat(health.getStatus()).isEqualTo(DOWN);
        assertThat(health.getDetails().get("message")).isEqualTo("Most recent valid Meterstand was saved at 2017-02-05T09:54:59. Which is more than 5 minutes ago.");
    }

}