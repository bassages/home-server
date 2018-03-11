package nl.homeserver.energie;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.energie.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.DatePeriod;
import nl.homeserver.LoggingRule;
import nl.homeserver.MessageContaining;
import nl.homeserver.cache.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class OpgenomenVermogenServiceTest {

    @InjectMocks
    private OpgenomenVermogenService opgenomenVermogenService;

    @Mock
    private OpgenomenVermogenRepository opgenomenVermogenRepository;
    @Mock
    private CacheService cacheService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<List<OpgenomenVermogen>> deletedOpgenomenVermogenCaptor;

    @Rule
    public LoggingRule loggingRule = new LoggingRule(OpgenomenVermogenService.class);

    @Test
    public void whenDailyCleanupThenCacheCleared() {
        timeTravelTo(clock, LocalDate.of(2017, JANUARY, 12).atStartOfDay());

        opgenomenVermogenService.dailyCleanup();

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

        opgenomenVermogenService.cleanup(date);

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

        opgenomenVermogenService.cleanup(date);

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

        opgenomenVermogenService.cleanup(date);

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

        opgenomenVermogenService.cleanup(date);

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

        opgenomenVermogenService.cleanup(date);

        assertThat(loggingRule.getLoggedEventCaptor().getAllValues()).isEmpty();
    }

    @Test
    public void givenOneDayPeriodWhenGetHistoryPerHalfDayThenMaxOpgenomenVermogensPerHalfDayReturned() {
        LocalDate day = LocalDate.of(2018, JANUARY, 6);
        DatePeriod period = aPeriodWithToDate(day, day.plusDays(1));

        OpgenomenVermogen opgenomenVermogenInFirstHalfOfDay1 = aOpgenomenVermogen().withDatumTijd(day.atTime(0, 0)).withWatt(100).build();
        OpgenomenVermogen opgenomenVermogenInFirstHalfOfDay2 = aOpgenomenVermogen().withDatumTijd(day.atTime(2, 0)).withWatt(401).build();
        OpgenomenVermogen opgenomenVermogenInFirstHalfOfDay3 = aOpgenomenVermogen().withDatumTijd(day.atTime(11, 59)).withWatt(400).build();

        OpgenomenVermogen opgenomenVermogenInSecondHalfOfDay1 = aOpgenomenVermogen().withDatumTijd(day.atTime(12, 0)).withWatt(500).build();
        OpgenomenVermogen opgenomenVermogenInSecondHalfOfDay2 = aOpgenomenVermogen().withDatumTijd(day.atTime(14, 0)).withWatt(601).build();
        OpgenomenVermogen opgenomenVermogenInSecondHalfOfDay3 = aOpgenomenVermogen().withDatumTijd(day.atTime(23, 59)).withWatt(600).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay()))
                                        .thenReturn(asList(opgenomenVermogenInFirstHalfOfDay1, opgenomenVermogenInFirstHalfOfDay2, opgenomenVermogenInFirstHalfOfDay3,
                                                           opgenomenVermogenInSecondHalfOfDay1, opgenomenVermogenInSecondHalfOfDay2, opgenomenVermogenInSecondHalfOfDay3));

        List<OpgenomenVermogen> history = opgenomenVermogenService.getHistory(period, Duration.ofHours(12));

        assertThat(history).extracting(OpgenomenVermogen::getDatumtijd, OpgenomenVermogen::getWatt)
                           .containsExactly(tuple(day.atTime(0, 0), 401),
                                            tuple(day.atTime(12, 0), 601),
                                            tuple(day.plusDays(1).atTime(0, 0), 0));
    }

    @Test
    public void whenGetMostRecentThenDelegatedToRepository() {
        OpgenomenVermogen mostRecent = mock(OpgenomenVermogen.class);

        when(opgenomenVermogenRepository.getMeestRecente()).thenReturn(mostRecent);

        assertThat(opgenomenVermogenService.getMostRecent()).isSameAs(mostRecent);
    }

    @Test
    public void whenSaveThenSavedInRepositoryAndMessageSendToTopic() {
        OpgenomenVermogen opgenomenVermogen = mock(OpgenomenVermogen.class);

        when(opgenomenVermogenRepository.save(any(OpgenomenVermogen.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
        opgenomenVermogenService.save(opgenomenVermogen);

        verify(messagingTemplate).convertAndSend(OpgenomenVermogenService.TOPIC, opgenomenVermogen);
    }

    @Test
    public void whenGetPotentiallyCachedHistoryThenReturned() {
        LocalDate day = LocalDate.of(2018, JANUARY, 6);
        DatePeriod period = aPeriodWithToDate(day, day.plusDays(1));

        when(opgenomenVermogenRepository.getOpgenomenVermogen(period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay()))
                .thenReturn(emptyList());

        List<OpgenomenVermogen> history = opgenomenVermogenService.getPotentiallyCachedHistory(period, Duration.ofHours(4));

        assertThat(history).extracting(OpgenomenVermogen::getDatumtijd, OpgenomenVermogen::getWatt)
                .containsExactly(tuple(day.atTime(0, 0), 0),
                                 tuple(day.atTime(4, 0), 0),
                                 tuple(day.atTime(8, 0), 0),
                                 tuple(day.atTime(12, 0), 0),
                                 tuple(day.atTime(16, 0), 0),
                                 tuple(day.atTime(20, 0), 0),
                                 tuple(day.plusDays(1).atTime(0, 0), 0));
    }
}