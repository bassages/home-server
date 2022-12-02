package nl.homeserver.energy.verbruikkosten;

import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energy.StroomTariefIndicator;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

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

    public VerbruikForVirtualUsageProvider(final int watt) {
        this.watt = watt;
    }

    @Override
    public BigDecimal getStroomVerbruik(final DateTimePeriod period,
                                        final StroomTariefIndicator stroomTariefIndicator) {

        final DatePeriod datePeriod = period.toDatePeriod();

        final long numberOfHours = switch (stroomTariefIndicator) {
            case DAL -> getNumberOfHoursLow(datePeriod);
            case NORMAAL -> getNumberOfHoursHigh(datePeriod);
            default -> throw new IllegalArgumentException("Unknown stroomTariefIndicator: " + stroomTariefIndicator);
        };
        return new BigDecimal(numberOfHours * watt).divide(BigDecimal.valueOf(1000), 3, HALF_UP);
    }

    @Override
    public BigDecimal getGasVerbruik(final DateTimePeriod period) {
        return ZERO;
    }

    private long getNumberOfHoursLow(final DatePeriod period) {
        return getNumberOfHours(period, NUMBER_OF_HOURS_LOW_ON_WEEKENDDAY, NUMBER_OF_HOURS_LOW_ON_WEEKDAY);
    }

    private long getNumberOfHoursHigh(final DatePeriod period) {
        return getNumberOfHours(period, NUMBER_OF_HOURS_HIGH_ON_WEEKENDDAY, NUMBER_OF_HOURS_HIGH_ON_WEEKDAY);
    }

    private long getNumberOfHours(final DatePeriod period,
                                  final int numberOfHoursHighOnWeekendday,
                                  final int numberOfHoursHighOnWeekday) {
        return (period.getNumberOfWeekendDays() * numberOfHoursHighOnWeekendday) +
                (period.getNumberOfWeekDays() * numberOfHoursHighOnWeekday);
    }
}
