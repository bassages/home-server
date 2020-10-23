package nl.homeserver.energie.meterstand;

import nl.homeserver.DatePeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterstandControllerTest {

    @InjectMocks
    MeterstandController meterstandController;

    @Mock
    MeterstandService meterstandService;

    @Test
    void whenGetMostRecentThenDelegatedToMeterstandService() {
        final Meterstand mostRecentMeterstand = mock(Meterstand.class);

        when(meterstandService.getMostRecent()).thenReturn(mostRecentMeterstand);

        assertThat(meterstandController.getMostRecent()).isEqualTo(mostRecentMeterstand);
    }

    @Test
    void whenGetOldestOfTodayThenDelegatedToMeterstandService() {
        final Meterstand oldestMeterstandOfToday = mock(Meterstand.class);

        when(meterstandService.getOldestOfToday()).thenReturn(oldestMeterstandOfToday);

        assertThat(meterstandController.getOldestOfToday()).isEqualTo(oldestMeterstandOfToday);
    }

    @Test
    void whenGetPerDagThenDelegatedToMeterstandService() {
        final LocalDate from = LocalDate.of(2017, JANUARY, 1);
        final LocalDate to = LocalDate.of(2018, FEBRUARY, 2);

        final List<MeterstandOpDag> meterstandenByDay = List.of(mock(MeterstandOpDag.class), mock(MeterstandOpDag.class));
        when(meterstandService.getPerDag(eq(DatePeriod.aPeriodWithToDate(from, to)))).thenReturn(meterstandenByDay);

        assertThat(meterstandController.perDag(from, to)).isEqualTo(meterstandenByDay);
    }
}
