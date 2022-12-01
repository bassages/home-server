package nl.homeserver.energie.meterstand;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

import static java.time.Month.DECEMBER;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeterstandCacheWarmerTest {

    @InjectMocks
    MeterstandCacheWarmer meterstandCacheWarmer;

    @Mock
    MeterstandController meterstandController;
    @Mock
    Clock clock;

    @Captor
    ArgumentCaptor<LocalDate> fromDateCaptor;
    @Captor
    ArgumentCaptor<LocalDate> toDateCaptor;

    @Test
    void whenWarmupCacheOnStartupThenMeterstandenPerDagWarmedUp() {
        timeTravelTo(clock, LocalDate.of(2017, 12, 30).atTime(13, 20));

        meterstandCacheWarmer.warmupCacheOnStartup();

        verify(meterstandController, times(13)).perDag(fromDateCaptor.capture(), toDateCaptor.capture());

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2016, 12, 1),
                LocalDate.of(2017, 1, 1),
                LocalDate.of(2017, 2, 1),
                LocalDate.of(2017, 3, 1),
                LocalDate.of(2017, 4, 1),
                LocalDate.of(2017, 5, 1),
                LocalDate.of(2017, 6, 1),
                LocalDate.of(2017, 7, 1),
                LocalDate.of(2017, 8, 1),
                LocalDate.of(2017, 9, 1),
                LocalDate.of(2017, 10, 1),
                LocalDate.of(2017, 11, 1),
                LocalDate.of(2017, 12, 1)
        );

        assertThat(toDateCaptor.getAllValues()).containsExactly(
                YearMonth.of(2016, 12).atEndOfMonth(),
                YearMonth.of(2017, 1).atEndOfMonth(),
                YearMonth.of(2017, 2).atEndOfMonth(),
                YearMonth.of(2017, 3).atEndOfMonth(),
                YearMonth.of(2017, 4).atEndOfMonth(),
                YearMonth.of(2017, 5).atEndOfMonth(),
                YearMonth.of(2017, 6).atEndOfMonth(),
                YearMonth.of(2017, 7).atEndOfMonth(),
                YearMonth.of(2017, 8).atEndOfMonth(),
                YearMonth.of(2017, 9).atEndOfMonth(),
                YearMonth.of(2017, 10).atEndOfMonth(),
                YearMonth.of(2017, 11).atEndOfMonth(),
                YearMonth.of(2017, 12).atEndOfMonth()
        );
    }

    @Test
    void whenWarmupCacheDailyThenMeterstandenPerDagWarmedUp() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        meterstandCacheWarmer.warmupCacheDaily();

        verify(meterstandController).perDag(LocalDate.of(2017, DECEMBER, 1), LocalDate.of(2017, DECEMBER, 31));
    }
}
