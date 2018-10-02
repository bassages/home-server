package nl.homeserver.energie.standbypower;

import static java.time.Month.JUNE;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StandbyPowerCacheWarmerTest {

    @InjectMocks
    private StandbyPowerCacheWarmer standbyPowerCacheWarmer;

    @Mock
    private StandbyPowerController standbyPowerController;
    @Mock
    private Clock clock;

    @Test
    public void whenWarmupInitialCacheThenStandbyPowerCacheWarmedUp() {
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        standbyPowerCacheWarmer.warmupInitialCache();

        verify(standbyPowerController).getStandbyPower(2017);
        verify(standbyPowerController).getStandbyPower(2016);
    }

    @Test
    public void whenWarmupDailyCacheThenStandbyPowerCacheWarmedUp() {
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        standbyPowerCacheWarmer.warmupDailyCache();

        verify(standbyPowerController).getStandbyPower(2017);
        verify(standbyPowerController).getStandbyPower(2016);
    }
}