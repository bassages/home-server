package nl.homeserver.energie;

import java.math.BigDecimal;

import nl.homeserver.DateTimePeriod;

public interface VerbruikProvider {

    BigDecimal getStroomVerbruik(DateTimePeriod period,
                                 StroomTariefIndicator stroomTariefIndicator);

    BigDecimal getGasVerbruik(DateTimePeriod period);
}