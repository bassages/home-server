package nl.homeserver.energie.verbruikkosten;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.SEPTEMBER;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

class VerbruikForVirtualUsageProviderTest {

    private static final LocalDate WEEKDAY = LocalDate.of(2018, SEPTEMBER, 12);
    private static final LocalDate WEEKENDDAY = LocalDate.of(2018, SEPTEMBER, 15);

    @Test
    void whenGetGasVerbruikThenZeroReturned() {
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(100);

        final BigDecimal gasVerbruik = verbruikForVirtualUsageProvider.getGasVerbruik(mock(DateTimePeriod.class));

        assertThat(gasVerbruik).isEqualTo(ZERO);
    }

    @Test
    void whenGetStroomVerbruikNormaalForWeekDayThenReturned() {
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);

        final DateTimePeriod period = aPeriodWithToDateTime(WEEKDAY.atStartOfDay(), WEEKDAY.plusDays(1).atStartOfDay());

        final BigDecimal stroomVerbruik = verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.NORMAAL);

        assertThat(stroomVerbruik).isEqualTo(new BigDecimal("16.000"));
    }

    @Test
    void whenGetStroomVerbruikDalForWeekDayThenReturned() {
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);

        final DateTimePeriod period = aPeriodWithToDateTime(WEEKDAY.atStartOfDay(), WEEKDAY.plusDays(1).atStartOfDay());

        final BigDecimal stroomVerbruik = verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.DAL);

        assertThat(stroomVerbruik).isEqualTo(new BigDecimal("8.000"));
    }

    @Test
    void whenGetStroomVerbruikNormaalForWeekendDayThenReturned() {
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);

        final DateTimePeriod period = aPeriodWithToDateTime(WEEKENDDAY.atStartOfDay(), WEEKENDDAY.plusDays(1)
                                                                                                 .atStartOfDay());

        final BigDecimal stroomVerbruik = verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.NORMAAL);

        assertThat(stroomVerbruik).isEqualTo(new BigDecimal("0.000"));
    }

    @Test
    void whenGetStroomVerbruikDalForWeekendDayThenReturned() {
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);

        final DateTimePeriod period = aPeriodWithToDateTime(WEEKENDDAY.atStartOfDay(), WEEKENDDAY.plusDays(1).atStartOfDay());

        final BigDecimal stroomVerbruik = verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.DAL);

        assertThat(stroomVerbruik).isEqualTo(new BigDecimal("24.000"));
    }

    @Test
    void whenGetStroomVerbruikForUnexpectedStroomTariefIndicatorThenExceptio() {
        final VerbruikForVirtualUsageProvider verbruikForVirtualUsageProvider = new VerbruikForVirtualUsageProvider(1000);
        final DateTimePeriod period = aPeriodWithToDateTime(WEEKENDDAY.atStartOfDay(), WEEKENDDAY.plusDays(1).atStartOfDay());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> verbruikForVirtualUsageProvider.getStroomVerbruik(period, StroomTariefIndicator.ONBEKEND))
                .withMessage("Unknown stroomTariefIndicator: ONBEKEND");
    }
}
