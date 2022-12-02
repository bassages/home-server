package nl.homeserver.energy.opgenomenvermogen;

import nl.homeserver.DatePeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.time.Month.*;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpgenomenVermogenControllerTest {

    @InjectMocks
    OpgenomenVermogenController opgenomenVermogenController;

    @Mock
    OpgenomenVermogenService opgenomenVermogenService;
    @Mock
    Clock clock;

    @Test
    void whenGetMostRecentThenDelegatedToService() {
        // given
        final OpgenomenVermogen opgenomenVermogen = mock(OpgenomenVermogen.class);
        when(opgenomenVermogenService.getMostRecent()).thenReturn(opgenomenVermogen);

        // when
        final OpgenomenVermogen mostRecent = opgenomenVermogenController.getMostRecent();

        // then
        assertThat(mostRecent).isSameAs(opgenomenVermogen);
    }

    @Test
    void whenGetOpgenomenVermogenHistoryInPastThenDelegatedToCachedService() {
        // given
        timeTravelTo(clock, LocalDate.of(2019, MARCH, 5).atStartOfDay());

        final LocalDate from = LocalDate.of(2017, JANUARY, 1);
        final LocalDate to = LocalDate.of(2018, FEBRUARY, 1);

        final long subPeriodLengthInSeconds = 1;
        final List<OpgenomenVermogen> opgenomenVermogens = List.of(
                mock(OpgenomenVermogen.class), mock(OpgenomenVermogen.class));
        when(opgenomenVermogenService.getPotentiallyCachedHistory(DatePeriod.aPeriodWithToDate(from, to),
                                                                  Duration.ofSeconds(1)))
                                     .thenReturn(opgenomenVermogens);

        // when
        final List<OpgenomenVermogen> opgenomenVermogenHistory = opgenomenVermogenController.getOpgenomenVermogenHistory(from, to, subPeriodLengthInSeconds);

        // then
        assertThat(opgenomenVermogenHistory).isSameAs(opgenomenVermogens);
    }

    @Test
    void whenGetOpgenomenVermogenHistoryOfTodayThenDelegatedToNotCachedService() {
        // given
        timeTravelTo(clock, LocalDate.of(2018, JANUARY, 1).atStartOfDay());

        final LocalDate from = LocalDate.of(2018, JANUARY, 1);
        final LocalDate to = LocalDate.of(2018, JANUARY, 1);

        final long subPeriodLengthInSeconds = 1;
        final List<OpgenomenVermogen> opgenomenVermogens = List.of(
                mock(OpgenomenVermogen.class), mock(OpgenomenVermogen.class));
        when(opgenomenVermogenService.getHistory(DatePeriod.aPeriodWithToDate(from, to),
                                                 Duration.ofSeconds(1)))
                                     .thenReturn(opgenomenVermogens);

        // when
        final List<OpgenomenVermogen> opgenomenVermogenHistory = opgenomenVermogenController.getOpgenomenVermogenHistory(from, to, subPeriodLengthInSeconds);

        // then
        assertThat(opgenomenVermogenHistory).isSameAs(opgenomenVermogens);
    }
}
