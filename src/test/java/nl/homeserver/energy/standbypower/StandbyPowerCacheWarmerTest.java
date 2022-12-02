package nl.homeserver.energy.standbypower;

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
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class StandbyPowerCacheWarmerTest {

    @InjectMocks
    StandbyPowerCacheWarmer standbyPowerCacheWarmer;

    @Mock
    StandbyPowerController standbyPowerController;
    @Mock
    Clock clock;

    @Test
    void whenWarmupCacheOnStartupThenStandbyPowerCacheWarmedUp() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        // when
        standbyPowerCacheWarmer.warmupCacheOnStartup();

        // then
        verify(standbyPowerController).getStandbyPower(2017);
        verify(standbyPowerController).getStandbyPower(2016);
        verifyNoMoreInteractions(standbyPowerController);
    }

    @Test
    void whenWarmupCacheDailyThenStandbyPowerCacheWarmedUp() {
        // given
        timeTravelTo(clock, LocalDate.of(2017, JUNE, 30).atStartOfDay());

        // when
        standbyPowerCacheWarmer.warmupCacheDaily();

        // then
        verify(standbyPowerController).getStandbyPower(2017);
        verify(standbyPowerController).getStandbyPower(2016);
        verifyNoMoreInteractions(standbyPowerController);
    }
}
