package nl.wiegman.home.energie;

import static java.util.Arrays.asList;
import static nl.wiegman.home.DatePeriod.aPeriodWithToDate;
import static nl.wiegman.home.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpgenomenVermogenControllerTest {

    private OpgenomenVermogenController opgenomenVermogenController;

    @Mock
    private OpgenomenVermogenService opgenomenVermogenService;

    private void createOpgenomenVermogenController(Clock clock) {
        opgenomenVermogenController = new OpgenomenVermogenController(opgenomenVermogenService, clock);
    }

    @Test
    public void whenGetMostRecentThenDelegatedToService() {
        createOpgenomenVermogenController(timeTravelTo(LocalDate.of(2017, 3, 5).atStartOfDay()));

        OpgenomenVermogen mostRecent = mock(OpgenomenVermogen.class);
        when(opgenomenVermogenService.getMostRecent()).thenReturn(mostRecent);

        assertThat(opgenomenVermogenController.getMostRecent()).isSameAs(mostRecent);
    }

    @Test
    public void whenGetOpgenomenVermogenHistoryInPastThenDelegatedToCachedService() {
        createOpgenomenVermogenController(timeTravelTo(LocalDate.of(2019, 3, 5).atStartOfDay()));

        LocalDate from = LocalDate.of(2017, 1, 1);
        LocalDate to = LocalDate.of(2018, 2, 1);

        long subPeriodLength = 1000;
        List<OpgenomenVermogen> opgenomenVermogens = asList(mock(OpgenomenVermogen.class), mock(OpgenomenVermogen.class));
        when(opgenomenVermogenService.getPotentiallyCachedHistory(eq(aPeriodWithToDate(from, to)), eq(subPeriodLength))).thenReturn(opgenomenVermogens);

        assertThat(opgenomenVermogenController.getOpgenomenVermogenHistory(from, to, subPeriodLength)).isSameAs(opgenomenVermogens);
    }

    @Test
    public void whenGetOpgenomenVermogenHistoryOfTodayThenDelegatedToNotCachedService() {
        createOpgenomenVermogenController(timeTravelTo(LocalDate.of(2018, 1, 1).atStartOfDay()));

        LocalDate from = LocalDate.of(2018, 1, 1);
        LocalDate to = LocalDate.of(2018, 1, 1);

        long subPeriodLength = 1000;
        List<OpgenomenVermogen> opgenomenVermogens = asList(mock(OpgenomenVermogen.class), mock(OpgenomenVermogen.class));
        when(opgenomenVermogenService.getHistory(eq(aPeriodWithToDate(from, to)), eq(subPeriodLength))).thenReturn(opgenomenVermogens);

        assertThat(opgenomenVermogenController.getOpgenomenVermogenHistory(from, to, subPeriodLength)).isSameAs(opgenomenVermogens);
    }

}