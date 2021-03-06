package nl.homeserver.energie.opgenomenvermogen;

import nl.homeserver.DatePeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.time.Month.JANUARY;
import static java.util.Collections.emptyList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpgenomenVermogenServiceTest {

    @InjectMocks
    OpgenomenVermogenService opgenomenVermogenService;

    @Mock
    OpgenomenVermogenRepository opgenomenVermogenRepository;
    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Test
    void givenOneDayPeriodWhenGetHistoryPerHalfDayThenMaxOpgenomenVermogensPerHalfDayReturned() {
        final LocalDate day = LocalDate.of(2018, JANUARY, 6);
        final DatePeriod period = aPeriodWithToDate(day, day.plusDays(1));

        final OpgenomenVermogen opgenomenVermogenInFirstHalfOfDay1 = aOpgenomenVermogen().withDatumTijd(day.atTime(0, 0)).withWatt(100).build();
        final OpgenomenVermogen opgenomenVermogenInFirstHalfOfDay2 = aOpgenomenVermogen().withDatumTijd(day.atTime(2, 0)).withWatt(401).build();
        final OpgenomenVermogen opgenomenVermogenInFirstHalfOfDay3 = aOpgenomenVermogen().withDatumTijd(day.atTime(11, 59)).withWatt(400).build();

        final OpgenomenVermogen opgenomenVermogenInSecondHalfOfDay1 = aOpgenomenVermogen().withDatumTijd(day.atTime(12, 0)).withWatt(500).build();
        final OpgenomenVermogen opgenomenVermogenInSecondHalfOfDay2 = aOpgenomenVermogen().withDatumTijd(day.atTime(14, 0)).withWatt(601).build();
        final OpgenomenVermogen opgenomenVermogenInSecondHalfOfDay3 = aOpgenomenVermogen().withDatumTijd(day.atTime(23, 59)).withWatt(600).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay()))
                                        .thenReturn(List.of(opgenomenVermogenInFirstHalfOfDay1, opgenomenVermogenInFirstHalfOfDay2, opgenomenVermogenInFirstHalfOfDay3,
                                                           opgenomenVermogenInSecondHalfOfDay1, opgenomenVermogenInSecondHalfOfDay2, opgenomenVermogenInSecondHalfOfDay3));

        final List<OpgenomenVermogen> history = opgenomenVermogenService.getHistory(period, Duration.ofHours(12));

        assertThat(history).extracting(OpgenomenVermogen::getDatumtijd, OpgenomenVermogen::getWatt)
                           .containsExactly(tuple(day.atTime(0, 0), 401),
                                            tuple(day.atTime(12, 0), 601),
                                            tuple(day.plusDays(1).atTime(0, 0), 0));
    }

    @Test
    void whenGetMostRecentThenDelegatedToRepository() {
        final OpgenomenVermogen mostRecent = mock(OpgenomenVermogen.class);

        when(opgenomenVermogenRepository.getMostRecent()).thenReturn(mostRecent);

        assertThat(opgenomenVermogenService.getMostRecent()).isSameAs(mostRecent);
    }

    @Test
    void whenSaveThenSavedInRepositoryAndMessageSendToTopic() {
        final OpgenomenVermogen opgenomenVermogen = mock(OpgenomenVermogen.class);

        when(opgenomenVermogenRepository.save(any(OpgenomenVermogen.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
        opgenomenVermogenService.save(opgenomenVermogen);

        verify(messagingTemplate).convertAndSend(OpgenomenVermogenService.TOPIC, opgenomenVermogen);
    }

    @Test
    void whenGetPotentiallyCachedHistoryThenReturned() {
        final LocalDate day = LocalDate.of(2018, JANUARY, 6);
        final DatePeriod period = aPeriodWithToDate(day, day.plusDays(1));

        when(opgenomenVermogenRepository.getOpgenomenVermogen(period.getFromDate().atStartOfDay(),
                                                              period.getToDate().atStartOfDay()))
                                        .thenReturn(emptyList());

        final List<OpgenomenVermogen> history = opgenomenVermogenService.getPotentiallyCachedHistory(period, Duration.ofHours(4));

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
