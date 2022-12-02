package nl.homeserver.energy.verbruikkosten;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energy.StroomTariefIndicator;

import java.math.BigDecimal;

public interface VerbruikProvider {

    BigDecimal getStroomVerbruik(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator);

    BigDecimal getGasVerbruik(DateTimePeriod period);
}