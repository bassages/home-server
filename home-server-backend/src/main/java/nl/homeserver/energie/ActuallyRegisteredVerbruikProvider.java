package nl.homeserver.energie;

import static java.lang.String.format;
import static nl.homeserver.energie.StroomTariefIndicator.DAL;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import nl.homeserver.DateTimePeriod;

// TODO: unittest
@Component
public class ActuallyRegisteredVerbruikProvider implements VerbruikProvider {

    private final VerbruikRepository verbruikRepository;

    public ActuallyRegisteredVerbruikProvider(final VerbruikRepository verbruikRepository) {
        this.verbruikRepository = verbruikRepository;
    }

    @Override
    public BigDecimal getStroomVerbruik(final DateTimePeriod period,
                                         final StroomTariefIndicator stroomTariefIndicator) {
        if (stroomTariefIndicator == DAL) {
            return verbruikRepository.getStroomVerbruikDalTariefInPeriod(period.getFromDateTime(), period.getToDateTime());
        } else if (stroomTariefIndicator == NORMAAL) {
            return verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(period.getFromDateTime(), period.getToDateTime());
        } else {
            throw new IllegalArgumentException(format("Unexpected StroomTariefIndicator: [%s]", stroomTariefIndicator));
        }
    }

    @Override
    public BigDecimal getGasVerbruik(final DateTimePeriod period) {
        // Gas is registered once every hour, in the hour after it actually is used.
        // Compensate for that hour to make the query return the correct usages.
        return verbruikRepository.getGasVerbruikInPeriod(period.getFromDateTime().plusHours(1), period.getToDateTime().plusHours(1));
    }
}
