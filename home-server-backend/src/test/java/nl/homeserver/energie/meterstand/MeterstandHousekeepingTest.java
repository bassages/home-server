package nl.homeserver.energie.meterstand;

import static ch.qos.logback.classic.Level.DEBUG;
import static java.time.Month.JANUARY;
import static nl.homeserver.energie.meterstand.MeterstandBuilder.aMeterstand;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static nl.homeserver.util.TimeMachine.useSystemDefaultClock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.LoggingRule;
import nl.homeserver.MessageContaining;
import nl.homeserver.cache.CacheService;
import nl.homeserver.energie.verbruikkosten.VerbruikKostenOverzichtService;

@RunWith(MockitoJUnitRunner.class)
public class MeterstandHousekeepingTest {

    @InjectMocks
    private MeterstandHousekeeping meterstandHousekeeping;

    @Mock
    private MeterstandRepository meterstandRepository;
    @Mock
    private CacheService cacheService;
    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<List<Meterstand>> deletedMeterstandCaptor;

    @Rule
    public final LoggingRule loggingRule = new LoggingRule(MeterstandHousekeeping.class);

    @Test
    public void whenCleanUpThenCachesCleared() {
        useSystemDefaultClock(clock);

        meterstandHousekeeping.start();

        verify(cacheService).clear(VerbruikKostenOverzichtService.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
        verify(cacheService).clear(VerbruikKostenOverzichtService.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
    }

    @Test
    public void whenStartThenPastMonthIsCleanedUp() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        meterstandHousekeeping.start();

        verify(meterstandRepository).findDatesBeforeToDateWithMoreRowsThan(currentDateTime.toLocalDate().minusMonths(1),
                currentDateTime.toLocalDate(), 48);
    }

    @Test
    public void givenOnlyASingleMeterstandExistsWhenCleanupThenNoneDeleted() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                 .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final Meterstand meterstand = aMeterstand().withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        when(meterstandRepository.findByDateTimeBetween(any(), any())).thenReturn(List.of(meterstand));

        meterstandHousekeeping.start();

        verify(meterstandRepository).findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt());
        verify(meterstandRepository).findByDateTimeBetween(any(), any());
        verifyNoMoreInteractions(meterstandRepository);
    }

    @Test
    public void whenCleanupThenAllButFirstAndLastMeterstandPerHourAreDeleted() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                 .thenReturn(List.of(dayToCleanupAsTimeStamp));

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

        verify(meterstandRepository, times(2)).deleteInBatch(deletedMeterstandCaptor.capture());

        assertThat(deletedMeterstandCaptor.getAllValues().get(0)).containsExactly(meterstand2, meterstand3);
        assertThat(deletedMeterstandCaptor.getAllValues().get(1)).containsExactly(meterstand6, meterstand7);
    }

    @Test
    public void whenCleanupThenKeptAndDeletedMeterstandenAreLogged() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                 .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final Meterstand meterstand1 = aMeterstand().withId(1).withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        final Meterstand meterstand2 = aMeterstand().withId(2).withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        final Meterstand meterstand3 = aMeterstand().withId(3).withDateTime(dayToCleanup.atTime(12, 30, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(List.of(meterstand1, meterstand2, meterstand3));

        loggingRule.setLevel(DEBUG);

        meterstandHousekeeping.start();

        final List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents)
                .haveExactly(1, new MessageContaining("[DEBUG] Keep first in hour 12: Meterstand[dateTime=2016-01-01T12:00,gas=0.000,id=1"))
                .haveExactly(1, new MessageContaining("[DEBUG] Keep last in hour 12: Meterstand[dateTime=2016-01-01T12:30,gas=0.000,id=3"))
                .haveExactly(1, new MessageContaining("[DEBUG] Delete: Meterstand[dateTime=2016-01-01T12:15,gas=0.000,id=2"));
    }

    @Test
    public void givenLogLevelIsOffWhenCleanupThenNothingLogged() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(meterstandRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final Meterstand meterstand1 = aMeterstand().withId(1).withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        final Meterstand meterstand2 = aMeterstand().withId(2).withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        final Meterstand meterstand3 = aMeterstand().withId(3).withDateTime(dayToCleanup.atTime(12, 30, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(List.of(meterstand1, meterstand2, meterstand3));

        loggingRule.setLevel(Level.OFF);

        meterstandHousekeeping.start();

        assertThat(loggingRule.getLoggedEventCaptor().getAllValues()).isEmpty();
    }
}
