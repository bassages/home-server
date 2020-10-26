package nl.homeserver.klimaat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.JUNE;
import static nl.homeserver.klimaat.KlimaatSensorBuilder.aKlimaatSensor;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KlimaatCacheWarmerTest {

    @InjectMocks
    KlimaatCacheWarmer klimaatCacheWarmer;

    @Mock
    KlimaatController klimaatController;
    @Mock
    Clock clock;

    @Captor
    ArgumentCaptor<LocalDate> fromDateCaptor;
    @Captor
    ArgumentCaptor<LocalDate> toDateCaptor;

    @Test
    void whenWarmupInitialCacheThenClimatePerDayWarmedup() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        final String sensorCode = "SOME_NICE_CODE";
        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(sensorCode).build();
        when(klimaatController.getAllKlimaatSensors()).thenReturn(List.of(klimaatSensor));

        klimaatCacheWarmer.warmupInitialCache();

        verify(klimaatController, times(7)).findAllInPeriod(eq(sensorCode),
                fromDateCaptor.capture(), toDateCaptor.capture());

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
    void whenWarmupInitialCacheThenClimateAveragesWarmedup() {
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        final String sensorCode = "SOME_NICE_CODE";

        when(klimaatController.getAllKlimaatSensors())
                .thenReturn(List.of(aKlimaatSensor().withCode(sensorCode).build()));

        klimaatCacheWarmer.warmupInitialCache();

        verify(klimaatController).getAverage(eq(sensorCode),
                                             eq(SensorType.TEMPERATUUR.name()),
                                             AdditionalMatchers.aryEq(new int[]{2017, 2016, 2015}));
        verify(klimaatController).getAverage(eq(sensorCode),
                                             eq(SensorType.LUCHTVOCHTIGHEID.name()),
                                             AdditionalMatchers.aryEq(new int[]{2017, 2016, 2015}));
    }

    @Test
    void whenWarmupDailyCacheThenClimatePerDayWarmedup() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        final String sensorCode = "SOME_FANCY_SENSOR";
        final KlimaatSensor klimaatSensor = aKlimaatSensor().withCode(sensorCode).build();
        when(klimaatController.getAllKlimaatSensors()).thenReturn(List.of(klimaatSensor));

        klimaatCacheWarmer.warmupDailyCache();

        verify(klimaatController).findAllInPeriod(sensorCode,
                LocalDate.of(2017, DECEMBER, 29), LocalDate.of(2017, DECEMBER, 30));
    }

}
