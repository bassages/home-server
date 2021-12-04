package nl.homeserver.energie.verbruikkosten;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import nl.homeserver.energie.energycontract.EnergyContract;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static org.apache.commons.lang3.ObjectUtils.max;
import static org.apache.commons.lang3.ObjectUtils.min;

@RequiredArgsConstructor
@Service
public class VerbruikKostenService {

    public VerbruikKosten getGasVerbruikKosten(final VerbruikProvider verbruikProvider,
                                               final EnergyContract energyContract,
                                               final DateTimePeriod period) {
        final DateTimePeriod subPeriod = getSubPeriod(energyContract, period);

        final BigDecimal verbruik = verbruikProvider.getGasVerbruik(subPeriod);

        BigDecimal kosten = null;
        if (verbruik != null) {
            kosten = energyContract.getGasPerKuub().multiply(verbruik);
        }
        return new VerbruikKosten(verbruik, kosten);
    }

    public VerbruikKosten getStroomVerbruikKosten(final VerbruikProvider verbruikProvider,
                                                  final EnergyContract energyContract,
                                                  final StroomTariefIndicator stroomTariefIndicator,
                                                  final DateTimePeriod period) {
        final DateTimePeriod subPeriod = getSubPeriod(energyContract, period);

        final BigDecimal verbruik = verbruikProvider.getStroomVerbruik(subPeriod, stroomTariefIndicator);

        BigDecimal kosten = null;
        if (verbruik != null) {
            kosten = energyContract.getStroomKosten(stroomTariefIndicator).multiply(verbruik);
        }
        return new VerbruikKosten(verbruik, kosten);
    }

    private DateTimePeriod getSubPeriod(final EnergyContract energyContract, final DateTimePeriod period) {
        final LocalDateTime subFrom = max(period.getFromDateTime(), energyContract.getValidFrom().atStartOfDay());
        final LocalDateTime subTo = min(period.getToDateTime(), energyContract.getValidTo() != null ? energyContract.getValidTo().atStartOfDay() : null);
        return aPeriodWithToDateTime(subFrom, subTo);
    }
}
