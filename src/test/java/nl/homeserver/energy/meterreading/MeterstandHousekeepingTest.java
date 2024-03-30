package nl.homeserver.energy.meterreading;

import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.CaptureLogging;
import nl.homeserver.ContainsMessageAtLevel;
import nl.homeserver.cache.CacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static ch.qos.logback.classic.Level.DEBUG;
import static java.time.Month.JANUARY;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE;
import static nl.homeserver.energy.meterreading.MeterstandBuilder.aMeterstand;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static nl.homeserver.util.TimeMachine.useSystemDefaultClock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeterstandHousekeepingTest {

    @InjectMocks
    MeterstandHousekeeping meterstandHousekeeping;

    @Mock
    MeterstandRepository meterstandRepository;
    @Mock
    CacheService cacheService;
    @Mock
    Clock clock;

    @Captor
    ArgumentCaptor<List<Meterstand>> deletedMeterstandCaptor;

    @Test
    void whenCleanUpThenCachesCleared() {
        // given
        useSystemDefaultClock(clock);

        // when
        meterstandHousekeeping.start();

        // then
        verify(cacheService).clear(CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
        verify(cacheService).clear(CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
    }

    @Test
    void whenStartThenPastMonthIsCleanedUp() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        meterstandHousekeeping.start();

        verify(meterstandRepository).findDatesBeforeToDateWithMoreRowsThan(currentDateTime.toLocalDate().minusMonths(1),
                currentDateTime.toLocalDate(), 48);
    }

    @Test
    void givenOnlyASingleMeterstandExistsWhenCleanupThenNoneDeleted() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Date dayToCleanupAsSqlDate = mock(Date.class);
        when(dayToCleanupAsSqlDate.toLocalDate()).thenReturn(dayToCleanup);
        when(meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                 .thenReturn(List.of(dayToCleanupAsSqlDate));

        final Meterstand meterstand = aMeterstand().withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        when(meterstandRepository.findByDateTimeBetween(any(), any())).thenReturn(List.of(meterstand));

        meterstandHousekeeping.start();

        verify(meterstandRepository).findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt());
        verify(meterstandRepository).findByDateTimeBetween(any(), any());
        verifyNoMoreInteractions(meterstandRepository);
    }

    @Test
    void whenCleanupThenAllButFirstAndLastMeterstandPerHourAreDeleted() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Date dayToCleanupAsSqlDate = mock(Date.class);
        when(dayToCleanupAsSqlDate.toLocalDate()).thenReturn(dayToCleanup);
        when(meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                 .thenReturn(List.of(dayToCleanupAsSqlDate));

        final Meterstand meterstand1 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        final Meterstand meterstand2 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        final Meterstand meterstand3 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 30, 0)).build();
        final Meterstand meterstand4 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 45, 0)).build();

        final Meterstand meterstand5 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 0, 0)).build();
        final Meterstand meterstand6 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 15, 0)).build();
        final Meterstand meterstand7 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 30, 0)).build();
        final Meterstand meterstand8 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 45, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(List.of(meterstand1, meterstand2, meterstand3, meterstand4, meterstand5, meterstand6, meterstand7, meterstand8));

        meterstandHousekeeping.start();

        verify(meterstandRepository, times(2)).deleteAllInBatch(deletedMeterstandCaptor.capture());

        assertThat(deletedMeterstandCaptor.getAllValues()).satisfiesExactly(
                deletedMeterstandenHour12 -> assertThat(deletedMeterstandenHour12).containsExactly(meterstand2, meterstand3),
                deletedMeterstandenHour13 -> assertThat(deletedMeterstandenHour13).containsExactly(meterstand6, meterstand7)
        );
    }

    @CaptureLogging(MeterstandHousekeeping.class)
    @Test
    void whenCleanupThenKeptAndDeletedMeterstandenAreLogged(final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Date dayToCleanupAsSqlDate = mock(Date.class);
        when(dayToCleanupAsSqlDate.toLocalDate()).thenReturn(dayToCleanup);
        when(meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                 .thenReturn(List.of(dayToCleanupAsSqlDate));

        final Meterstand meterstand1 = aMeterstand().withId(1).withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        final Meterstand meterstand2 = aMeterstand().withId(2).withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        final Meterstand meterstand3 = aMeterstand().withId(3).withDateTime(dayToCleanup.atTime(12, 30, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(List.of(meterstand1, meterstand2, meterstand3));

        meterstandHousekeeping.start();

        assertThat(loggerEventCaptor.getAllValues())
                .haveExactly(1, new ContainsMessageAtLevel("Keep first in hour 12: Meterstand(id=1, dateTime=2016-01-01T12:00, date=2016-01-01, stroomTarief1=0.000, stroomTarief2=0.000, gas=0.000, stroomTariefIndicator=ONBEKEND)", DEBUG))
                .haveExactly(1, new ContainsMessageAtLevel("Keep last in hour 12: Meterstand(id=3, dateTime=2016-01-01T12:30, date=2016-01-01, stroomTarief1=0.000, stroomTarief2=0.000, gas=0.000, stroomTariefIndicator=ONBEKEND)", DEBUG))
                .haveExactly(1, new ContainsMessageAtLevel("Delete: Meterstand(id=2, dateTime=2016-01-01T12:15, date=2016-01-01, stroomTarief1=0.000, stroomTarief2=0.000, gas=0.000, stroomTariefIndicator=ONBEKEND)", DEBUG));
    }
}
