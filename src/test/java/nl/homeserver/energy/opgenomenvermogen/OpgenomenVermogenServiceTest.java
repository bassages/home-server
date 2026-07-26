package nl.homeserver.energy.opgenomenvermogen;

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
import static nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogenBuilder.aOpgenomenVermogen;
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
        // given
        final LocalDate day = LocalDate.of(2018, JANUARY, 6);
        final DatePeriod period = DatePeriod.of(day);

        final OpgenomenVermogen opgenomenVermogenInFirstHalfOfDay1 = aOpgenomenVermogen().withDatumTijd(day.atTime(0, 0)).withActivePowerTotalInWatts(100).withActivePowerL1InWatts(30).withActivePowerL2InWatts(30).withActivePowerL3InWatts(40).build();
        final OpgenomenVermogen opgenomenVermogenInFirstHalfOfDay2 = aOpgenomenVermogen().withDatumTijd(day.atTime(2, 0)).withActivePowerTotalInWatts(401).withActivePowerL1InWatts(101).withActivePowerL2InWatts(100).withActivePowerL3InWatts(200).build();
        final OpgenomenVermogen opgenomenVermogenInFirstHalfOfDay3 = aOpgenomenVermogen().withDatumTijd(day.atTime(11, 59)).withActivePowerTotalInWatts(400).withActivePowerL1InWatts(100).withActivePowerL2InWatts(100).withActivePowerL3InWatts(200).build();

        final OpgenomenVermogen opgenomenVermogenInSecondHalfOfDay1 = aOpgenomenVermogen().withDatumTijd(day.atTime(12, 0)).withActivePowerTotalInWatts(500).withActivePowerL1InWatts(100).withActivePowerL2InWatts(150).withActivePowerL3InWatts(250).build();
        final OpgenomenVermogen opgenomenVermogenInSecondHalfOfDay2 = aOpgenomenVermogen().withDatumTijd(day.atTime(14, 0)).withActivePowerTotalInWatts(601).withActivePowerL1InWatts(201).withActivePowerL2InWatts(200).withActivePowerL3InWatts(200).build();
        final OpgenomenVermogen opgenomenVermogenInSecondHalfOfDay3 = aOpgenomenVermogen().withDatumTijd(day.atTime(23, 59)).withActivePowerTotalInWatts(600).withActivePowerL1InWatts(200).withActivePowerL2InWatts(200).withActivePowerL3InWatts(200).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(period.getFromDate().atStartOfDay(), period.getToDate().atStartOfDay()))
                                        .thenReturn(List.of(opgenomenVermogenInFirstHalfOfDay1, opgenomenVermogenInFirstHalfOfDay2, opgenomenVermogenInFirstHalfOfDay3,
                                                           opgenomenVermogenInSecondHalfOfDay1, opgenomenVermogenInSecondHalfOfDay2, opgenomenVermogenInSecondHalfOfDay3));

        // when
        final List<OpgenomenVermogen> history = opgenomenVermogenService.getHistory(period, Duration.ofHours(12));

        // then
        assertThat(history).extracting(OpgenomenVermogen::getDatumtijd,
                                       OpgenomenVermogen::getActivePowerTotalInWatts,
                                       OpgenomenVermogen::getActivePowerL1InWatts,
                                       OpgenomenVermogen::getActivePowerL2InWatts,
                                       OpgenomenVermogen::getActivePowerL3InWatts)
                           .containsExactly(tuple(day.atTime(0, 0), 401, 101, 100, 200),
                                            tuple(day.atTime(12, 0), 601, 201, 200, 200),
                                            tuple(day.plusDays(1).atTime(0, 0), 0, 0, 0, 0));
    }

    @Test
    void whenGetMostRecentThenDelegatedToRepository() {
        // given
        final OpgenomenVermogen mostRecent = mock(OpgenomenVermogen.class);
        when(opgenomenVermogenRepository.getMostRecent()).thenReturn(mostRecent);

        // when
        final OpgenomenVermogen result = opgenomenVermogenService.getMostRecent();

        // then
        assertThat(result).isSameAs(mostRecent);
    }

    @Test
    void whenSaveThenSavedInRepositoryAndMessageSendToTopic() {
        // given
        final OpgenomenVermogen opgenomenVermogen = mock(OpgenomenVermogen.class);
        when(opgenomenVermogenRepository.save(any(OpgenomenVermogen.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        // when
        opgenomenVermogenService.save(opgenomenVermogen);

        // then
        verify(messagingTemplate).convertAndSend(OpgenomenVermogenService.TOPIC, opgenomenVermogen);
    }

    @Test
    void whenGetPotentiallyCachedHistoryThenReturned() {
        // given
        final LocalDate day = LocalDate.of(2018, JANUARY, 6);
        final DatePeriod period = DatePeriod.of(day);

        when(opgenomenVermogenRepository.getOpgenomenVermogen(period.getFromDate().atStartOfDay(),
                                                              period.getToDate().atStartOfDay()))
                                        .thenReturn(emptyList());

        // when
        final List<OpgenomenVermogen> history = opgenomenVermogenService.getPotentiallyCachedHistory(period, Duration.ofHours(4));

        // then
        assertThat(history).extracting(OpgenomenVermogen::getDatumtijd,
                                       OpgenomenVermogen::getActivePowerTotalInWatts,
                                       OpgenomenVermogen::getActivePowerL1InWatts,
                                       OpgenomenVermogen::getActivePowerL2InWatts,
                                       OpgenomenVermogen::getActivePowerL3InWatts)
                .containsExactly(tuple(day.atTime(0, 0), 0, 0, 0, 0),
                                 tuple(day.atTime(4, 0), 0, 0, 0, 0),
                                 tuple(day.atTime(8, 0), 0, 0, 0, 0),
                                 tuple(day.atTime(12, 0), 0, 0, 0, 0),
                                 tuple(day.atTime(16, 0), 0, 0, 0, 0),
                                 tuple(day.atTime(20, 0), 0, 0, 0, 0),
                                 tuple(day.plusDays(1).atTime(0, 0), 0, 0, 0, 0));
    }
}
