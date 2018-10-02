package nl.homeserver.energie.verbruikkosten;

import static java.time.Month.DECEMBER;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VerbruikKostenCacheWarmerTest {

    @InjectMocks
    private VerbruikKostenCacheWarmer verbruikKostenCacheWarmer;

    @Mock
    private VerbruikKostenController verbruikKostenController;
    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<LocalDate> fromDateCaptor;
    @Captor
    private ArgumentCaptor<LocalDate> toDateCaptor;
    @Captor
    private ArgumentCaptor<LocalDate> dateCaptor;
    @Captor
    private ArgumentCaptor<Integer> yearCaptor;

    @Test
    public void whenWarmupInitialCacheThenVerbruikPerUurOpDagWarmedup() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        verbruikKostenCacheWarmer.warmupInitialCache();

        verify(verbruikKostenController, times(14)).getVerbruikPerUurOpDag(dateCaptor.capture());

        assertThat(dateCaptor.getAllValues()).containsExactly(
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
    }

    @Test
    public void whenWarmupInitialCacheThenVerbruikPerDagWarmedup() {
        timeTravelTo(clock, LocalDate.of(2017, 12, 30).atTime(13, 20));

        verbruikKostenCacheWarmer.warmupInitialCache();

        verify(verbruikKostenController, times(13)).getVerbruikPerDag(fromDateCaptor.capture(), toDateCaptor.capture());

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
    public void whenWarmupInitialCacheThenVerbruikPerMaandInJaarWarmedup() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        verbruikKostenCacheWarmer.warmupInitialCache();

        verify(verbruikKostenController, times(2)).getVerbruikPerMaandInJaar(yearCaptor.capture());

        assertThat(yearCaptor.getAllValues()).containsExactly(2016, 2017);
    }

    @Test
    public void whenWarmupInitialCacheThenVerbruikPerJaarWarmedup() {
        timeTravelTo(clock, LocalDate.of(2017, DECEMBER, 30).atTime(13, 20));

        verbruikKostenCacheWarmer.warmupInitialCache();

        verify(verbruikKostenController).getVerbruikPerJaar();
    }
}