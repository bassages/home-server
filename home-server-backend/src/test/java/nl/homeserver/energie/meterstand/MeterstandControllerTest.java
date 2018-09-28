package nl.homeserver.energie.meterstand;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homeserver.DatePeriod;
import nl.homeserver.energie.meterstand.Meterstand;
import nl.homeserver.energie.meterstand.MeterstandController;
import nl.homeserver.energie.meterstand.MeterstandOpDag;
import nl.homeserver.energie.meterstand.MeterstandService;

@RunWith(MockitoJUnitRunner.class)
public class MeterstandControllerTest {

    @InjectMocks
    private MeterstandController meterstandController;

    @Mock
    private MeterstandService meterstandService;

    @Test
    public void whenGetMostRecentThenDelegatedToMeterstandService() {
        final Meterstand mostRecentMeterstand = mock(Meterstand.class);

        when(meterstandService.getMostRecent()).thenReturn(mostRecentMeterstand);

        assertThat(meterstandController.getMostRecent()).isEqualTo(mostRecentMeterstand);
    }

    @Test
    public void whenGetOldestOfTodayThenDelegatedToMeterstandService() {
        final Meterstand oldestMeterstandOfToday = mock(Meterstand.class);

        when(meterstandService.getOldestOfToday()).thenReturn(oldestMeterstandOfToday);

        assertThat(meterstandController.getOldestOfToday()).isEqualTo(oldestMeterstandOfToday);
    }

    @Test
    public void whenGetPerDagThenDelegatedToMeterstandService() {
        final LocalDate from = LocalDate.of(2017, JANUARY, 1);
        final LocalDate to = LocalDate.of(2018, FEBRUARY, 2);

        final List<MeterstandOpDag> meterstandenByDay = List.of(mock(MeterstandOpDag.class), mock(MeterstandOpDag.class));
        when(meterstandService.getPerDag(eq(DatePeriod.aPeriodWithToDate(from, to)))).thenReturn(meterstandenByDay);

        Assertions.assertThat(meterstandController.perDag(from, to)).isEqualTo(meterstandenByDay);
    }
}