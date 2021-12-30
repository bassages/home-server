package nl.homeserver.energie.verbruikkosten;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor
public class ActuallyRegisteredVerbruikProvider implements VerbruikProvider {

    private final VerbruikRepository verbruikRepository;

    @Override
    public BigDecimal getStroomVerbruik(final DateTimePeriod period,
                                        final StroomTariefIndicator stroomTariefIndicator) {
        return switch (stroomTariefIndicator) {
            case DAL -> verbruikRepository.getStroomVerbruikDalTariefInPeriod(period.getFromDateTime(), period.getToDateTime());
            case NORMAAL -> verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(period.getFromDateTime(), period.getToDateTime());
            default -> throw new IllegalArgumentException(format("Unexpected StroomTariefIndicator: [%s]", stroomTariefIndicator));
        };
    }

    @Override
    public BigDecimal getGasVerbruik(final DateTimePeriod period) {
        // Gas is registered once every hour, in the hour after it actually is used.
        // Compensate for that hour to make the query return the correct usages.
        return verbruikRepository.getGasVerbruikInPeriod(period.getFromDateTime().plusHours(1), period.getToDateTime().plusHours(1));
    }
}
