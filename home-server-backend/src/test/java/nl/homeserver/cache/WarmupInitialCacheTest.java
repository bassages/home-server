package nl.homeserver.cache;

import nl.homeserver.energie.EnergieController;
import nl.homeserver.energie.MeterstandController;
import nl.homeserver.energie.OpgenomenVermogenController;
import nl.homeserver.klimaat.KlimaatController;
import nl.homeserver.klimaat.KlimaatSensor;
import nl.homeserver.klimaat.KlimaatService;
import nl.homeserver.klimaat.SensorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

import static java.time.Month.DECEMBER;
import static java.time.Month.JUNE;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static nl.homeserver.klimaat.KlimaatSensorBuilder.aKlimaatSensor;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class WarmupInitialCacheTest {

    @InjectMocks
    private WarmupInitialCache warmupInitialCache;

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
    private Clock clock;

    @Captor
    private ArgumentCaptor<LocalDate> fromDateCaptor;
    @Captor
    private ArgumentCaptor<LocalDate> toDateCaptor;

    @Captor
    private ArgumentCaptor<LocalDate> dateCaptor;

    @Captor
    private ArgumentCaptor<Integer> yearCaptor;

    @Test
    public void givenWarmupDisabledWhenApplicationStartedThenNoWarmup() {
        setWarmupCacheDisabled();

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verifyZeroInteractions(klimaatController, opgenomenVermogenController, energieController, meterstandController);
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenOpgenomenVermogenHistoryWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(opgenomenVermogenController, times(14)).getOpgenomenVermogenHistory(fromDateCaptor.capture(),
                                                                                                           toDateCaptor.capture(),
                                                                                                           eq(MINUTES.toMillis(3)));

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 16),
                LocalDate.of(2017, DECEMBER, 17),
                LocalDate.of(2017, DECEMBER, 18),
                LocalDate.of(2017, DECEMBER, 19),
                LocalDate.of(2017, DECEMBER, 20),
                LocalDate.of(2017, DECEMBER, 21),
                LocalDate.of(2017, DECEMBER, 22),
                LocalDate.of(2017, DECEMBER, 23),
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29)
        );

        assertThat(toDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 17),
                LocalDate.of(2017, DECEMBER, 18),
                LocalDate.of(2017, DECEMBER, 19),
                LocalDate.of(2017, DECEMBER, 20),
                LocalDate.of(2017, DECEMBER, 21),
                LocalDate.of(2017, DECEMBER, 22),
                LocalDate.of(2017, DECEMBER, 23),
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29),
                LocalDate.of(2017, DECEMBER, 30)
        );
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenVerbruikPerUurOpDagWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(energieController, times(14)).getVerbruikPerUurOpDag(dateCaptor.capture());

        assertThat(dateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 16),
                LocalDate.of(2017, DECEMBER, 17),
                LocalDate.of(2017, DECEMBER, 18),
                LocalDate.of(2017, DECEMBER, 19),
                LocalDate.of(2017, DECEMBER, 20),
                LocalDate.of(2017, DECEMBER, 21),
                LocalDate.of(2017, DECEMBER, 22),
                LocalDate.of(2017, DECEMBER, 23),
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29)
        );
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenVerbruikPerDagWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, 12, 30).atTime(13, 20));

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(energieController, times(13)).getVerbruikPerDag(fromDateCaptor.capture(), toDateCaptor.capture());

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2016, 12, 1),
                LocalDate.of(2017, 1, 1),
                LocalDate.of(2017, 2, 1),
                LocalDate.of(2017, 3, 1),
                LocalDate.of(2017, 4, 1),
                LocalDate.of(2017, 5, 1),
                LocalDate.of(2017, 6, 1),
                LocalDate.of(2017, 7, 1),
                LocalDate.of(2017, 8, 1),
                LocalDate.of(2017, 9, 1),
                LocalDate.of(2017, 10, 1),
                LocalDate.of(2017, 11, 1),
                LocalDate.of(2017, 12, 1)
        );

        assertThat(toDateCaptor.getAllValues()).containsExactly(
                YearMonth.of(2016, 12).atEndOfMonth(),
                YearMonth.of(2017, 1).atEndOfMonth(),
                YearMonth.of(2017, 2).atEndOfMonth(),
                YearMonth.of(2017, 3).atEndOfMonth(),
                YearMonth.of(2017, 4).atEndOfMonth(),
                YearMonth.of(2017, 5).atEndOfMonth(),
                YearMonth.of(2017, 6).atEndOfMonth(),
                YearMonth.of(2017, 7).atEndOfMonth(),
                YearMonth.of(2017, 8).atEndOfMonth(),
                YearMonth.of(2017, 9).atEndOfMonth(),
                YearMonth.of(2017, 10).atEndOfMonth(),
                YearMonth.of(2017, 11).atEndOfMonth(),
                YearMonth.of(2017, 12).atEndOfMonth()
        );
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenMeterstandenPerDagWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, 12, 30).atTime(13, 20));

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(meterstandController, times(13)).perDag(fromDateCaptor.capture(), toDateCaptor.capture());

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2016, 12, 1),
                LocalDate.of(2017, 1, 1),
                LocalDate.of(2017, 2, 1),
                LocalDate.of(2017, 3, 1),
                LocalDate.of(2017, 4, 1),
                LocalDate.of(2017, 5, 1),
                LocalDate.of(2017, 6, 1),
                LocalDate.of(2017, 7, 1),
                LocalDate.of(2017, 8, 1),
                LocalDate.of(2017, 9, 1),
                LocalDate.of(2017, 10, 1),
                LocalDate.of(2017, 11, 1),
                LocalDate.of(2017, 12, 1)
        );

        assertThat(toDateCaptor.getAllValues()).containsExactly(
                YearMonth.of(2016, 12).atEndOfMonth(),
                YearMonth.of(2017, 1).atEndOfMonth(),
                YearMonth.of(2017, 2).atEndOfMonth(),
                YearMonth.of(2017, 3).atEndOfMonth(),
                YearMonth.of(2017, 4).atEndOfMonth(),
                YearMonth.of(2017, 5).atEndOfMonth(),
                YearMonth.of(2017, 6).atEndOfMonth(),
                YearMonth.of(2017, 7).atEndOfMonth(),
                YearMonth.of(2017, 8).atEndOfMonth(),
                YearMonth.of(2017, 9).atEndOfMonth(),
                YearMonth.of(2017, 10).atEndOfMonth(),
                YearMonth.of(2017, 11).atEndOfMonth(),
                YearMonth.of(2017, 12).atEndOfMonth()
        );
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenVerbruikPerMaandInJaarWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(energieController, times(2)).getVerbruikPerMaandInJaar(yearCaptor.capture());

        assertThat(yearCaptor.getAllValues()).containsExactly(2016, 2017);
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenVerbruikPerJaarWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(energieController).getVerbruikPerJaar();
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenClimatePerDayWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        final String sensorCode = "SOME_NICE_CODE";
        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(sensorCode).build();
        when(klimaatService.getAllKlimaatSensors()).thenReturn(singletonList(klimaatSensor));

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(klimaatController, times(7)).findAllInPeriod(eq(sensorCode), fromDateCaptor.capture(), toDateCaptor.capture());

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 23),
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29)
        );

        assertThat(toDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29),
                LocalDate.of(2017, DECEMBER, 30)
        );
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenClimateAveragesWarmedup() {
        setWarmupCacheEnabled();
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        final String sensorCode = "SOME_NICE_CODE";

        when(klimaatService.getAllKlimaatSensors())
                           .thenReturn(singletonList(aKlimaatSensor().withCode(sensorCode).build()));

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(klimaatController).getAverage(eq(sensorCode),
                                             eq(SensorType.TEMPERATUUR.name()),
                                             AdditionalMatchers.aryEq(new int[]{2017, 2016, 2015}));
        verify(klimaatController).getAverage(eq(sensorCode),
                                             eq(SensorType.LUCHTVOCHTIGHEID.name()),
                                             AdditionalMatchers.aryEq(new int[]{2017, 2016, 2015}));
    }

    private void setWarmupCacheDisabled() {
        setField(warmupInitialCache, "warmupCacheOnApplicationStart", false);
    }

    private void setWarmupCacheEnabled() {
        setField(warmupInitialCache, "warmupCacheOnApplicationStart", true);
    }
}