package nl.homeserver.energy.meterreading;

import nl.homeserver.DatePeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
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
        // given
        final Meterstand mostRecentMeterstand = mock(Meterstand.class);
        when(meterstandService.getMostRecent()).thenReturn(Optional.of(mostRecentMeterstand));

        // when
        final Meterstand mostRecent = meterstandController.getMostRecent();

        // then
        assertThat(mostRecent).isEqualTo(mostRecentMeterstand);
    }

    @Test
    void whenGetOldestOfTodayThenDelegatedToMeterstandService() {
        // given
        final Meterstand oldestMeterReadingOfToday = mock(Meterstand.class);
        when(meterstandService.findOldestOfToday()).thenReturn(Optional.of(oldestMeterReadingOfToday));

        // when
        final Meterstand oldestOfToday = meterstandController.getOldestOfToday();

        // then
        assertThat(oldestOfToday).isSameAs(oldestMeterReadingOfToday);
    }

    @Test
    void whenGetPerDagThenDelegatedToMeterstandService() {
        // given
        final LocalDate from = LocalDate.of(2017, JANUARY, 1);
        final LocalDate to = LocalDate.of(2018, FEBRUARY, 2);

        final List<MeterstandOpDag> meterReadingsPerDay = List.of(
                new MeterstandOpDag(LocalDate.now(), mock(Meterstand.class)),
                new MeterstandOpDag(LocalDate.now(), mock(Meterstand.class)));
        when(meterstandService.getPerDag(DatePeriod.aPeriodWithToDate(from, to))).thenReturn(meterReadingsPerDay);

        // when
        final List<MeterstandOpDag> actual = meterstandController.perDag(from, to);

        // then
        assertThat(actual).isSameAs(meterReadingsPerDay);
    }
}
