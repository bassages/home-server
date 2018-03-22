package nl.homeserver.energie;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.energie.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import nl.homeserver.DatePeriod;
import nl.homeserver.LoggingRule;

@RunWith(MockitoJUnitRunner.class)
public class OpgenomenVermogenServiceTest {

    @InjectMocks
    private OpgenomenVermogenService opgenomenVermogenService;

    @Mock
    private OpgenomenVermogenRepository opgenomenVermogenRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Rule
    public LoggingRule loggingRule = new LoggingRule(OpgenomenVermogenService.class);

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