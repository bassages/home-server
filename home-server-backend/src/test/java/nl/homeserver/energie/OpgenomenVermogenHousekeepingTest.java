package nl.homeserver.energie;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static nl.homeserver.energie.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static nl.homeserver.util.TimeMachine.useSystemDefaultClock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Captor
    private ArgumentCaptor<List<OpgenomenVermogen>> deletedOpgenomenVermogenCaptor;

    @Rule
    public LoggingRule loggingRule = new LoggingRule(OpgenomenVermogenHousekeeping.class);

    @Test
    public void whenDailyCleanupThenCacheCleared() {
        useSystemDefaultClock(clock);

        opgenomenVermogenHousekeeping.dailyCleanup();

        verify(cacheService).clear(OpgenomenVermogenService.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY);
    }

    @Test
    public void givengivenMultipleOpgenomenVermogenInOneMinuteWithSameWattWhenCleanupThenMostRecentInMinuteIsKept() {
        LocalDate date = LocalDate.of(2016, JANUARY, 1);

        OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 0)).withWatt(1).build();
        OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 10)).withWatt(1).build();
        OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(asList(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenHousekeeping.cleanup(date);

        verify(opgenomenVermogenRepository).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getValue()).containsExactlyInAnyOrder(opgenomenVermogen1, opgenomenVermogen2);
    }

    @Test
    public void givenMultipleOpgenomenVermogenInOneMinuteWithDifferentWattWhenCleanupThenHighestWattIsKept() {
        LocalDate date = LocalDate.of(2016, JANUARY, 1);

        OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 0)).withWatt(3).build();
        OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 10)).withWatt(2).build();
        OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(asList(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenHousekeeping.cleanup(date);

        verify(opgenomenVermogenRepository).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getValue()).containsExactlyInAnyOrder(opgenomenVermogen2, opgenomenVermogen3);
    }

    @Test
    public void whenCleanUpThenCleanUpPerMinuteAndDeletePerHour() {
        LocalDate date = LocalDate.of(2016, JANUARY, 1);

        OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withDatumTijd(date.atTime(12, 0, 0)).build();
        OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withDatumTijd(date.atTime(12, 0, 10)).build();

        OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withDatumTijd(date.atTime(12, 1, 0)).build();
        OpgenomenVermogen opgenomenVermogen4 = aOpgenomenVermogen().withDatumTijd(date.atTime(12, 1, 10)).build();

        OpgenomenVermogen opgenomenVermogen5 = aOpgenomenVermogen().withDatumTijd(date.atTime(13, 1, 0)).build();
        OpgenomenVermogen opgenomenVermogen6 = aOpgenomenVermogen().withDatumTijd(date.atTime(13, 1, 10)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(asList(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3, opgenomenVermogen4, opgenomenVermogen5, opgenomenVermogen6));

        opgenomenVermogenHousekeeping.cleanup(date);

        verify(opgenomenVermogenRepository, times(2)).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getAllValues().get(0)).containsExactlyInAnyOrder(opgenomenVermogen1, opgenomenVermogen3);
        assertThat(deletedOpgenomenVermogenCaptor.getAllValues().get(1)).containsExactlyInAnyOrder(opgenomenVermogen5);
    }

    @Test
    public void givenLogLevelIsInfoWhenCleanupThenKeptAndDeletedOpgenomensAreLoggedAtThatLevel() {
        LocalDate date = LocalDate.of(2016, JANUARY, 1);

        OpgenomenVermogen deleted = aOpgenomenVermogen().withId(1L).withDatumTijd(date.atTime(0, 0, 0)).build();
        OpgenomenVermogen kept = aOpgenomenVermogen().withId(2L).withDatumTijd(date.atTime(0, 0, 1)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(asList(deleted, kept));

        loggingRule.setLevel(Level.INFO);

        opgenomenVermogenHousekeeping.cleanup(date);

        List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[INFO] Keep: OpgenomenVermogen[id=2"));
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[INFO] Delete: OpgenomenVermogen[id=1"));
    }

    @Test
    public void givenLogLevelIsOffWhenCleanupThenKeptAndDeletedOpgenomensAreNotLogged() {
        LocalDate date = LocalDate.of(2016, JANUARY, 1);

        OpgenomenVermogen deleted = aOpgenomenVermogen().withId(1L).withDatumTijd(date.atTime(0, 0, 0)).build();
        OpgenomenVermogen kept = aOpgenomenVermogen().withId(2L).withDatumTijd(date.atTime(0, 0, 1)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(asList(deleted, kept));

        loggingRule.setLevel(Level.OFF);

        opgenomenVermogenHousekeeping.cleanup(date);

        assertThat(loggingRule.getLoggedEventCaptor().getAllValues()).isEmpty();
    }
}