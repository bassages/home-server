package nl.homeserver.energie.opgenomenvermogen;

import static java.time.Month.JANUARY;
import static nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static nl.homeserver.util.TimeMachine.useSystemDefaultClock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.LoggingRule;
import nl.homeserver.MessageContaining;
import nl.homeserver.cache.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class OpgenomenVermogenHousekeepingTest {

    @InjectMocks
    private OpgenomenVermogenHousekeeping opgenomenVermogenHousekeeping;

    @Mock
    private OpgenomenVermogenRepository opgenomenVermogenRepository;
    @Mock
    private CacheService cacheService;
    @Mock
    private Clock clock;

    @Rule
    public final LoggingRule loggingRule = new LoggingRule(OpgenomenVermogenHousekeeping.class);

    @Test
    public void whenDailyCleanupThenCacheCleared() {
        useSystemDefaultClock(clock);

        opgenomenVermogenHousekeeping.start();

        verify(cacheService).clear(OpgenomenVermogenService.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY);
    }

    @Test
    public void whenStartThenPastMonthIsCleanedUp() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        opgenomenVermogenHousekeeping.start();

        verify(opgenomenVermogenRepository).findDatesBeforeToDateWithMoreRowsThan(currentDateTime.toLocalDate().minusMonths(1),
                currentDateTime.toLocalDate(), 1440);
    }

    @Test
    public void givengivenMultipleOpgenomenVermogenInOneMinuteWithSameWattWhenCleanupThenMostRecentInMinuteIsKept() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withId(1).withDatumTijd(dayToCleanup.atTime(0, 0, 0)).withWatt(1).build();
        final OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withId(2).withDatumTijd(dayToCleanup.atTime(0, 0, 10)).withWatt(1).build();
        final OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withId(3).withDatumTijd(dayToCleanup.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                                        .thenReturn(List.of(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenHousekeeping.start();

        verify(opgenomenVermogenRepository).deleteById(opgenomenVermogen1.getId());
        verify(opgenomenVermogenRepository).deleteById(opgenomenVermogen2.getId());
    }

    @Test
    public void givenMultipleOpgenomenVermogenInOneMinuteWithDifferentWattWhenCleanupThenHighestWattIsKept() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withId(1).withDatumTijd(dayToCleanup.atTime(0, 0, 0)).withWatt(3).build();
        final OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withId(2).withDatumTijd(dayToCleanup.atTime(0, 0, 10)).withWatt(2).build();
        final OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withId(3).withDatumTijd(dayToCleanup.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                                        .thenReturn(List.of(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenHousekeeping.start();

        verify(opgenomenVermogenRepository).deleteById(opgenomenVermogen2.getId());
        verify(opgenomenVermogenRepository).deleteById(opgenomenVermogen3.getId());
    }

    @Test
    public void givenLogLevelIsDebugWhenCleanupThenKeptAndDeletedOpgenomensAreLoggedAtThatLevel() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen deleted = aOpgenomenVermogen().withId(1L).withDatumTijd(dayToCleanup.atTime(0, 0, 0)).build();
        final OpgenomenVermogen kept = aOpgenomenVermogen().withId(2L).withDatumTijd(dayToCleanup.atTime(0, 0, 1)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(deleted, kept));

        loggingRule.setLevel(Level.DEBUG);

        opgenomenVermogenHousekeeping.start();

        final List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents)
                .haveExactly(1, new MessageContaining("[DEBUG] Keep: OpgenomenVermogen[datumtijd=2016-01-01T00:00:01,id=2"))
                .haveExactly(1, new MessageContaining("[DEBUG] Delete: OpgenomenVermogen[datumtijd=2016-01-01T00:00,id=1"));
    }

    @Test
    public void givenLogLevelIsOffWhenCleanupThenKeptAndDeletedOpgenomensAreNotLogged() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen deleted = aOpgenomenVermogen().withId(1L).withDatumTijd(dayToCleanup.atTime(0, 0, 0)).build();
        final OpgenomenVermogen kept = aOpgenomenVermogen().withId(2L).withDatumTijd(dayToCleanup.atTime(0, 0, 1)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(deleted, kept));

        loggingRule.setLevel(Level.OFF);

        opgenomenVermogenHousekeeping.start();

        assertThat(loggingRule.getLoggedEventCaptor().getAllValues()).isEmpty();
    }
}
