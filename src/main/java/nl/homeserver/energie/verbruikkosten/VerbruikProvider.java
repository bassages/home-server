package nl.homeserver.energie.verbruikkosten;

import java.math.BigDecimal;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;

public interface VerbruikProvider {

    BigDecimal getStroomVerbruik(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator);

    BigDecimal getGasVerbruik(DateTimePeriod period);
}