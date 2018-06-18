package nl.homeserver.energie;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.LoggingRule;
import nl.homeserver.MessageContaining;
import nl.homeserver.cache.CacheService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static nl.homeserver.energie.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static nl.homeserver.util.TimeMachine.useSystemDefaultClock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

    @Captor
    private ArgumentCaptor<List<OpgenomenVermogen>> deletedOpgenomenVermogenCaptor;

    @Rule
    public LoggingRule loggingRule = new LoggingRule(OpgenomenVermogenHousekeeping.class);

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
                                        .thenReturn(singletonList(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(0, 0, 0)).withWatt(1).build();
        final OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(0, 0, 10)).withWatt(1).build();
        final OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                                        .thenReturn(asList(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenHousekeeping.start();

        verify(opgenomenVermogenRepository).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getValue()).containsExactlyInAnyOrder(opgenomenVermogen1, opgenomenVermogen2);
    }

    @Test
    public void givenMultipleOpgenomenVermogenInOneMinuteWithDifferentWattWhenCleanupThenHighestWattIsKept() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(singletonList(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(0, 0, 0)).withWatt(3).build();
        final OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(0, 0, 10)).withWatt(2).build();
        final OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                                        .thenReturn(asList(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenHousekeeping.start();

        verify(opgenomenVermogenRepository).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getValue()).containsExactlyInAnyOrder(opgenomenVermogen2, opgenomenVermogen3);
    }

    @Test
    public void whenCleanUpThenCleanUpPerMinuteAndDeletePerHour() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(singletonList(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(12, 0, 0)).build();
        final OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(12, 0, 10)).build();

        final OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(12, 1, 0)).build();
        final OpgenomenVermogen opgenomenVermogen4 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(12, 1, 10)).build();

        final OpgenomenVermogen opgenomenVermogen5 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(13, 1, 0)).build();
        final OpgenomenVermogen opgenomenVermogen6 = aOpgenomenVermogen().withDatumTijd(dayToCleanup.atTime(13, 1, 10)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                                        .thenReturn(asList(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3, opgenomenVermogen4, opgenomenVermogen5, opgenomenVermogen6));

        opgenomenVermogenHousekeeping.start();

        verify(opgenomenVermogenRepository, times(2)).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getAllValues().get(0)).containsExactlyInAnyOrder(opgenomenVermogen1, opgenomenVermogen3);
        assertThat(deletedOpgenomenVermogenCaptor.getAllValues().get(1)).containsExactlyInAnyOrder(opgenomenVermogen5);
    }

    @Test
    public void givenLogLevelIsDebugWhenCleanupThenKeptAndDeletedOpgenomensAreLoggedAtThatLevel() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(singletonList(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen deleted = aOpgenomenVermogen().withId(1L).withDatumTijd(dayToCleanup.atTime(0, 0, 0)).build();
        final OpgenomenVermogen kept = aOpgenomenVermogen().withId(2L).withDatumTijd(dayToCleanup.atTime(0, 0, 1)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                .thenReturn(asList(deleted, kept));

        loggingRule.setLevel(Level.DEBUG);

        opgenomenVermogenHousekeeping.start();

        final List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[DEBUG] Keep: OpgenomenVermogen[id=2"));
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[DEBUG] Delete: OpgenomenVermogen[id=1"));
    }

    @Test
    public void givenLogLevelIsOffWhenCleanupThenKeptAndDeletedOpgenomensAreNotLogged() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(singletonList(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen deleted = aOpgenomenVermogen().withId(1L).withDatumTijd(dayToCleanup.atTime(0, 0, 0)).build();
        final OpgenomenVermogen kept = aOpgenomenVermogen().withId(2L).withDatumTijd(dayToCleanup.atTime(0, 0, 1)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                .thenReturn(asList(deleted, kept));

        loggingRule.setLevel(Level.OFF);

        opgenomenVermogenHousekeeping.start();

        assertThat(loggingRule.getLoggedEventCaptor().getAllValues()).isEmpty();
    }
}