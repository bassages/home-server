package nl.homeserver.energie.verbruikkosten;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.SEPTEMBER;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class VerbruikForVirtualUsageProviderTest {

    private static final LocalDate WEEKDAY = LocalDate.of(2018, SEPTEMBER, 12);
    private static final LocalDate WEEKENDDAY = LocalDate.of(2018, SEPTEMBER, 15);

    @Test
    void whenGetGasVerbruikThenZeroReturned() {
        // given
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(100);

        // when
        final BigDecimal gasVerbruik = verbruikForVirtualUsageProvider.getGasVerbruik(mock(DateTimePeriod.class));

        // then
        assertThat(gasVerbruik).isEqualTo(ZERO);
    }

    @Test
    void whenGetStroomVerbruikNormaalForWeekDayThenReturned() {
        // given
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);
        final DateTimePeriod period = aPeriodWithToDateTime(WEEKDAY.atStartOfDay(), WEEKDAY.plusDays(1).atStartOfDay());

        // when
        final BigDecimal stroomVerbruik = verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.NORMAAL);

        // then
        assertThat(stroomVerbruik).isEqualTo(new BigDecimal("16.000"));
    }

    @Test
    void whenGetStroomVerbruikDalForWeekDayThenReturned() {
        // given
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);
        final DateTimePeriod period = aPeriodWithToDateTime(WEEKDAY.atStartOfDay(), WEEKDAY.plusDays(1).atStartOfDay());

        // when
        final BigDecimal stroomVerbruik = verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.DAL);

        // then
        assertThat(stroomVerbruik).isEqualTo(new BigDecimal("8.000"));
    }

    @Test
    void whenGetStroomVerbruikNormaalForWeekendDayThenReturned() {
        // given
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);
        final DateTimePeriod period = aPeriodWithToDateTime(WEEKENDDAY.atStartOfDay(), WEEKENDDAY.plusDays(1)
                                                                                                 .atStartOfDay());
        // when
        final BigDecimal stroomVerbruik = verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.NORMAAL);

        // then
        assertThat(stroomVerbruik).isEqualTo(new BigDecimal("0.000"));
    }

    @Test
    void whenGetStroomVerbruikDalForWeekendDayThenReturned() {
        // given
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);
        final DateTimePeriod period = aPeriodWithToDateTime(WEEKENDDAY.atStartOfDay(), WEEKENDDAY.plusDays(1).atStartOfDay());

        // when
        final BigDecimal stroomVerbruik = verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.DAL);

        // then
        assertThat(stroomVerbruik).isEqualTo(new BigDecimal("24.000"));
    }

    @Test
    void whenGetStroomVerbruikForUnexpectedStroomTariefIndicatorThenExceptio() {
        // given
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);
        final DateTimePeriod period = aPeriodWithToDateTime(WEEKENDDAY.atStartOfDay(), WEEKENDDAY.plusDays(1).atStartOfDay());

        // when
        assertThatThrownBy(() -> verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.ONBEKEND))
                // then
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown stroomTariefIndicator: ONBEKEND");
    }
}
