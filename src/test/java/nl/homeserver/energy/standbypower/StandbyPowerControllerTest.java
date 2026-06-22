package nl.homeserver.energy.standbypower;

import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, JANUARY))).thenReturn(Optional.of(standByPower1));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, FEBRUARY))).thenReturn(Optional.of(standByPower2));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, MARCH))).thenReturn(Optional.of(standByPower3));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, APRIL))).thenReturn(Optional.of(standByPower4));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, MAY))).thenReturn(Optional.of(standByPower5));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, JUNE))).thenReturn(Optional.of(standByPower6));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, JULY))).thenReturn(Optional.of(standByPower7));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, AUGUST))).thenReturn(Optional.of(standByPower8));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, SEPTEMBER))).thenReturn(Optional.of(standByPower9));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, OCTOBER))).thenReturn(Optional.of(standByPower10));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, NOVEMBER))).thenReturn(Optional.of(standByPower11));
        when(standbyPowerService.getStandbyPower(YearMonth.of(2018, DECEMBER))).thenReturn(Optional.of(standByPower12));

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
