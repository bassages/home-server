package nl.homeserver.energie;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.util.Arrays.asList;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.homeserver.DatePeriod;

@RunWith(MockitoJUnitRunner.class)
public class OpgenomenVermogenControllerTest {

    @InjectMocks
    private OpgenomenVermogenController opgenomenVermogenController;

    @Mock
    private OpgenomenVermogenService opgenomenVermogenService;
    @Mock
    private Clock clock;

    @Test
    public void whenGetMostRecentThenDelegatedToService() {
        timeTravelTo(clock, LocalDate.of(2017, MARCH, 5).atStartOfDay());

        OpgenomenVermogen mostRecent = mock(OpgenomenVermogen.class);
        when(opgenomenVermogenService.getMostRecent()).thenReturn(mostRecent);

        assertThat(opgenomenVermogenController.getMostRecent()).isSameAs(mostRecent);
    }

    @Test
    public void whenGetOpgenomenVermogenHistoryInPastThenDelegatedToCachedService() {
        timeTravelTo(clock, LocalDate.of(2019, MARCH, 5).atStartOfDay());

        LocalDate from = LocalDate.of(2017, JANUARY, 1);
        LocalDate to = LocalDate.of(2018, FEBRUARY, 1);

        long subPeriodLengthInSeconds = 1;
        List<OpgenomenVermogen> opgenomenVermogens = asList(mock(OpgenomenVermogen.class), mock(OpgenomenVermogen.class));
        when(opgenomenVermogenService.getPotentiallyCachedHistory(eq(DatePeriod.aPeriodWithToDate(from, to)), eq(Duration.ofSeconds(1)))).thenReturn(opgenomenVermogens);

        assertThat(opgenomenVermogenController.getOpgenomenVermogenHistory(from, to, subPeriodLengthInSeconds)).isSameAs(opgenomenVermogens);
    }

    @Test
    public void whenGetOpgenomenVermogenHistoryOfTodayThenDelegatedToNotCachedService() {
        timeTravelTo(clock, LocalDate.of(2018, JANUARY, 1).atStartOfDay());

        LocalDate from = LocalDate.of(2018, JANUARY, 1);
        LocalDate to = LocalDate.of(2018, JANUARY, 1);

        long subPeriodLengthInSeconds = 1;
        List<OpgenomenVermogen> opgenomenVermogens = asList(mock(OpgenomenVermogen.class), mock(OpgenomenVermogen.class));
        when(opgenomenVermogenService.getHistory(eq(DatePeriod.aPeriodWithToDate(from, to)), eq(Duration.ofSeconds(1)))).thenReturn(opgenomenVermogens);

        assertThat(opgenomenVermogenController.getOpgenomenVermogenHistory(from, to, subPeriodLengthInSeconds)).isSameAs(opgenomenVermogens);
    }
}