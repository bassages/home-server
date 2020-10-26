package nl.homeserver.energie.standbypower;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.Month.JUNE;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StandbyPowerCacheWarmerTest {

    @InjectMocks
    StandbyPowerCacheWarmer standbyPowerCacheWarmer;

    @Mock
    StandbyPowerController standbyPowerController;
    @Mock
    Clock clock;

    @Test
    void whenWarmupInitialCacheThenStandbyPowerCacheWarmedUp() {
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        standbyPowerCacheWarmer.warmupInitialCache();

        verify(standbyPowerController).getStandbyPower(2017);
        verify(standbyPowerController).getStandbyPower(2016);
    }

    @Test
    void whenWarmupDailyCacheThenStandbyPowerCacheWarmedUp() {
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        standbyPowerCacheWarmer.warmupDailyCache();

        verify(standbyPowerController).getStandbyPower(2017);
        verify(standbyPowerController).getStandbyPower(2016);
    }
}
