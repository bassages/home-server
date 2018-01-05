package nl.homeserver.energie;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.homeserver.DatePeriod;

@RunWith(MockitoJUnitRunner.class)
public class MeterstandControllerTest {

    @InjectMocks
    private MeterstandController meterstandController;

    @Mock
    private MeterstandService meterstandService;

    @Test
    public void whenGetMostRecentThenDelegatedToMeterstandService() {
        Meterstand mostRecentMeterstand = mock(Meterstand.class);

        when(meterstandService.getMostRecent()).thenReturn(mostRecentMeterstand);

        assertThat(meterstandController.getMostRecent()).isEqualTo(mostRecentMeterstand);
    }

    @Test
    public void whenGetOldestOfTodayThenDelegatedToMeterstandService() {
        Meterstand oldestMeterstandOfToday = mock(Meterstand.class);

        when(meterstandService.getOldestOfToday()).thenReturn(oldestMeterstandOfToday);

        assertThat(meterstandController.getOldestOfToday()).isEqualTo(oldestMeterstandOfToday);
    }

    @Test
    public void whenGetPerDagThenDelegatedToMeterstandService() {
        LocalDate from = LocalDate.of(2017, JANUARY, 1);
        LocalDate to = LocalDate.of(2018, FEBRUARY, 2);

        List<MeterstandOpDag> meterstandenByDay = asList(mock(MeterstandOpDag.class), mock(MeterstandOpDag.class));
        when(meterstandService.getPerDag(Matchers.eq(DatePeriod.aPeriodWithToDate(from, to)))).thenReturn(meterstandenByDay);

        Assertions.assertThat(meterstandController.perDag(from, to)).isEqualTo(meterstandenByDay);
    }
}