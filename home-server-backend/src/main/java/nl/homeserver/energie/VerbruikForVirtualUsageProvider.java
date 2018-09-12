package nl.homeserver.energie;

import static java.math.RoundingMode.HALF_UP;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.energie.StroomTariefIndicator.DAL;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;

import java.math.BigDecimal;

import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;

/**
 * Limburg en Noord-Brabant hebben een afwijkend tijdschema,
 * daar houd deze class geen rekening mee!
 */
public class VerbruikForVirtualUsageProvider implements VerbruikProvider {

    private static final int NUMBER_OF_HOURS_LOW_ON_WEEKDAY = 8;
    private static final int NUMBER_OF_HOURS_HIGH_ON_WEEKDAY = 16;

    private static final int NUMBER_OF_HOURS_LOW_ON_WEEKENDDAY = 24;
    private static final int NUMBER_OF_HOURS_HIGH_ON_WEEKENDDAY = 0;

    private final int watt;

    VerbruikForVirtualUsageProvider(final int watt) {
        this.watt = watt;
    }

    @Override
    public BigDecimal getStroomVerbruik(final DateTimePeriod period,
                                        final StroomTariefIndicator stroomTariefIndicator) {

        final DatePeriod datePeriod = aPeriodWithToDate(period.getFromDateTime().toLocalDate(),
                                                        period.getToDateTime().toLocalDate());

        final long numberOfHours;

        if (stroomTariefIndicator == NORMAAL) {
            numberOfHours = getNumberOfHoursHigh(datePeriod);
        } else if (stroomTariefIndicator == DAL) {
            numberOfHours = getNumberOfHoursLow(datePeriod);
        } else {
            throw new IllegalArgumentException("Unknown stroomTariefIndicator: " + stroomTariefIndicator);
        }
        return new BigDecimal(numberOfHours * watt).divide(BigDecimal.valueOf(1000), 3, HALF_UP);
    }

    @Override
    public BigDecimal getGasVerbruik(final DateTimePeriod period) {
        return BigDecimal.ZERO;
    }

    private long getNumberOfHoursLow(final DatePeriod period) {
        return (period.getNumberOfWeekendDays() * NUMBER_OF_HOURS_LOW_ON_WEEKENDDAY) +
                (period.getNumberOfWeekDays() * NUMBER_OF_HOURS_LOW_ON_WEEKDAY);
    }

    private long getNumberOfHoursHigh(final DatePeriod period) {
        return (period.getNumberOfWeekendDays() * NUMBER_OF_HOURS_HIGH_ON_WEEKENDDAY) +
                (period.getNumberOfWeekDays() * NUMBER_OF_HOURS_HIGH_ON_WEEKDAY);
    }
}
