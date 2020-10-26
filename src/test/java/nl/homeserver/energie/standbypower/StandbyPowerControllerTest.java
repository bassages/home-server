package nl.homeserver.energie.standbypower;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandbyPowerControllerTest {

    @InjectMocks
    StandbyPowerController standbyPowerController;

    @Mock
    StandbyPowerService standbyPowerService;
    @Mock
    Clock clock;

    @Test
    void whenGetStandbyPowerForYearThen12MonthsReturned() {
        final LocalDate date = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, date.atStartOfDay());

        final StandbyPowerInPeriod standByPower1 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower2 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower3 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower4 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower5 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower6 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower7 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower8 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower9 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower10 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower11 = mock(StandbyPowerInPeriod.class);
        final StandbyPowerInPeriod standByPower12 = mock(StandbyPowerInPeriod.class);

        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 1))).thenReturn(Optional.of(standByPower1));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 2))).thenReturn(Optional.of(standByPower2));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 3))).thenReturn(Optional.of(standByPower3));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 4))).thenReturn(Optional.of(standByPower4));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 5))).thenReturn(Optional.of(standByPower5));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 6))).thenReturn(Optional.of(standByPower6));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 7))).thenReturn(Optional.of(standByPower7));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 8))).thenReturn(Optional.of(standByPower8));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 9))).thenReturn(Optional.of(standByPower9));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 10))).thenReturn(Optional.of(standByPower10));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 11))).thenReturn(Optional.of(standByPower11));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, 12))).thenReturn(Optional.of(standByPower12));

        final List<StandbyPowerInPeriod> standbyPower = standbyPowerController.getStandbyPower(2018);

        assertThat(standbyPower).containsExactly(standByPower12,
                                                 standByPower11,
                                                 standByPower10,
                                                 standByPower9,
                                                 standByPower8,
                                                 standByPower7,
                                                 standByPower6,
                                                 standByPower5,
                                                 standByPower4,
                                                 standByPower3,
                                                 standByPower2,
                                                 standByPower1
        );
    }

}
