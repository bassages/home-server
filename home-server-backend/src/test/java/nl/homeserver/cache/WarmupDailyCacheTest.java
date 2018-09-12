package nl.homeserver.cache;

import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.SEPTEMBER;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static nl.homeserver.klimaat.KlimaatSensorBuilder.aKlimaatSensor;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.Clock;
import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homeserver.energie.EnergieController;
import nl.homeserver.energie.MeterstandController;
import nl.homeserver.energie.OpgenomenVermogenController;
import nl.homeserver.energie.StandbyPowerController;
import nl.homeserver.klimaat.KlimaatController;
import nl.homeserver.klimaat.KlimaatSensor;
import nl.homeserver.klimaat.KlimaatService;

@RunWith(MockitoJUnitRunner.class)
public class WarmupDailyCacheTest {

    @InjectMocks
    private WarmupDailyCache warmupDailyCache;

    @Mock
    private KlimaatController klimaatController;
    @Mock
    private KlimaatService klimaatService;
    @Mock
    private OpgenomenVermogenController opgenomenVermogenController;
    @Mock
    private EnergieController energieController;
    @Mock
    private MeterstandController meterstandController;
    @Mock
    private StandbyPowerController standbyPowerController;
    @Mock
    private Clock clock;

    @Test
    public void givenWarmupDisabledWhenConsideredThenNoWarmup() {
        setWarmupCacheDisabled();

        warmupDailyCache.considerDailyWarmup();

        verifyZeroInteractions(klimaatController, opgenomenVermogenController, energieController, meterstandController);
    }

    @Test
    public void givenWarmupEnabledWhenConsideredThenOpgenomenVermogenHistoryWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        warmupDailyCache.considerDailyWarmup();

        verify(opgenomenVermogenController).getOpgenomenVermogenHistory(LocalDate.of(2017, DECEMBER, 29),
                                                                        LocalDate.of(2017, DECEMBER, 30),
                                                                        MINUTES.toMillis(3));
    }

    @Test
    public void givenWarmupEnabledWhenConsideredThenVerbruikPerUurOpDagWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        warmupDailyCache.considerDailyWarmup();

        verify(energieController).getVerbruikPerUurOpDag(LocalDate.of(2017, 12, 29));
    }

    @Test
    public void givenWarmupEnabledWhenConsideredThenVerbruikPerDagWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, SEPTEMBER, 1).atTime(0, 5));

        warmupDailyCache.considerDailyWarmup();

        verify(energieController).getVerbruikPerDag(LocalDate.of(2017, AUGUST, 1), LocalDate.of(2017, AUGUST, 31));
    }

    @Test
    public void givenWarmupEnabledWhenConsideredThenMeterstandenPerDagWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        warmupDailyCache.considerDailyWarmup();

        verify(meterstandController).perDag(LocalDate.of(2017, DECEMBER, 1), LocalDate.of(2017, DECEMBER, 31));
    }

    @Test
    public void givenWarmupEnabledWhenConsideredThenVerbruikPerMaandInJaarWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, JANUARY, 1).atTime(0, 5));

        warmupDailyCache.considerDailyWarmup();

        verify(energieController).getVerbruikPerMaandInJaar(2016);
    }

    @Test
    public void givenWarmupEnabledWhenConsideredThenVerbruikPerJaarWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        warmupDailyCache.considerDailyWarmup();

        verify(energieController).getVerbruikPerJaar();
    }

    @Test
    public void givenWarmupEnabledWhenConsideredThenClimatePerDayWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        final String sensorCode = "SOME_FANCY_SENSOR";
        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(sensorCode).build();
        when(klimaatService.getAllKlimaatSensors()).thenReturn(singletonList(klimaatSensor));

        warmupDailyCache.considerDailyWarmup();

        verify(klimaatController).findAllInPeriod(sensorCode, LocalDate.of(2017, DECEMBER, 29), LocalDate.of(2017, DECEMBER, 30));
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenStandbyPowerCacheWarmedUp() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        warmupDailyCache.considerDailyWarmup();

        verify(standbyPowerController).getStandbyPower(2017);
        verify(standbyPowerController).getStandbyPower(2016);
    }

    private void setWarmupCacheDisabled() {
        setField(warmupDailyCache, "warmupCacheDaily", false);
    }

    private void setWarmupCacheEnabled() {
        setField(warmupDailyCache, "warmupCacheDaily", true);
    }
}