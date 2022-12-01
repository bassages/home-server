package nl.homeserver.energie.opgenomenvermogen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.Month.DECEMBER;
import static java.util.concurrent.TimeUnit.MINUTES;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OpgenomenVermogenCacheWarmerTest {

    @InjectMocks
    OpgenomenVermogenCacheWarmer opgenomenVermogenCacheWarmer;

    @Mock
    OpgenomenVermogenController opgenomenVermogenController;
    @Mock
    Clock clock;

    @Captor
    ArgumentCaptor<LocalDate> fromDateCaptor;
    @Captor
    ArgumentCaptor<LocalDate> toDateCaptor;

    @Test
    void whenWarmupCacheOnStartupThenOpgenomenVermogenHistoryWarmedUp() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        opgenomenVermogenCacheWarmer.warmupCacheOnStartup();

        verify(opgenomenVermogenController, times(14))
                .getOpgenomenVermogenHistory(fromDateCaptor.capture(),
                                             toDateCaptor.capture(),
                                             eq(MINUTES.toMillis(3)));

        assertThat(fromDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 16),
                LocalDate.of(2017, DECEMBER, 17),
                LocalDate.of(2017, DECEMBER, 18),
                LocalDate.of(2017, DECEMBER, 19),
                LocalDate.of(2017, DECEMBER, 20),
                LocalDate.of(2017, DECEMBER, 21),
                LocalDate.of(2017, DECEMBER, 22),
                LocalDate.of(2017, DECEMBER, 23),
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29)
        );

        assertThat(toDateCaptor.getAllValues()).containsExactly(
                LocalDate.of(2017, DECEMBER, 17),
                LocalDate.of(2017, DECEMBER, 18),
                LocalDate.of(2017, DECEMBER, 19),
                LocalDate.of(2017, DECEMBER, 20),
                LocalDate.of(2017, DECEMBER, 21),
                LocalDate.of(2017, DECEMBER, 22),
                LocalDate.of(2017, DECEMBER, 23),
                LocalDate.of(2017, DECEMBER, 24),
                LocalDate.of(2017, DECEMBER, 25),
                LocalDate.of(2017, DECEMBER, 26),
                LocalDate.of(2017, DECEMBER, 27),
                LocalDate.of(2017, DECEMBER, 28),
                LocalDate.of(2017, DECEMBER, 29),
                LocalDate.of(2017, DECEMBER, 30)
        );
    }

    @Test
    void whenWarmupCacheDailyThenOpgenomenVermogenHistoryWarmedUp() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(0, 5));

        opgenomenVermogenCacheWarmer.warmupCacheDaily();

        verify(opgenomenVermogenController).getOpgenomenVermogenHistory(
                LocalDate.of(2017, DECEMBER, 29),
                LocalDate.of(2017, DECEMBER, 30),
                MINUTES.toMillis(3));
    }
}
