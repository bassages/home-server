package nl.homeserver.energie.verbruikkosten;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import nl.homeserver.energie.energycontract.EnergyContract;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static org.apache.commons.lang3.ObjectUtils.max;
import static org.apache.commons.lang3.ObjectUtils.min;

@Service
@RequiredArgsConstructor
public class VerbruikKostenService {

    public VerbruikKosten getGasVerbruikKosten(final VerbruikProvider verbruikProvider,
                                               final EnergyContract energyContract,
                                               final DateTimePeriod period) {
        final DateTimePeriod subPeriod = getPeriodThatFallsIntoPeriodOfEnergyContract(energyContract, period);

        final Optional<BigDecimal> optionalVerbruik = Optional.ofNullable(verbruikProvider.getGasVerbruik(subPeriod));

        return optionalVerbruik
                .map(verbruik -> new VerbruikKosten(verbruik, energyContract.getGasPerKuub().multiply(verbruik)))
                .orElseGet(VerbruikKosten::empty);
    }

    public VerbruikKosten getStroomVerbruikKosten(final VerbruikProvider verbruikProvider,
                                                  final EnergyContract energyContract,
                                                  final StroomTariefIndicator stroomTariefIndicator,
                                                  final DateTimePeriod period) {
        final DateTimePeriod subPeriod = getPeriodThatFallsIntoPeriodOfEnergyContract(energyContract, period);

        final Optional<BigDecimal> optionalVerbruik = Optional.ofNullable(
                verbruikProvider.getStroomVerbruik(subPeriod, stroomTariefIndicator));

        return optionalVerbruik
                .map(verbruik -> new VerbruikKosten(
                        verbruik, energyContract.getStroomKosten(stroomTariefIndicator).multiply(verbruik)))
                .orElseGet(VerbruikKosten::empty);
    }

    private DateTimePeriod getPeriodThatFallsIntoPeriodOfEnergyContract(final EnergyContract energyContract,
                                                                        final DateTimePeriod period) {
        final LocalDateTime subFrom = max(period.getFromDateTime(), energyContract.getValidFrom().atStartOfDay());
        final LocalDateTime subTo = min(period.getToDateTime(),
                energyContract.getValidTo() != null ? energyContract.getValidTo().atStartOfDay() : null);
        return aPeriodWithToDateTime(subFrom, subTo);
    }
}
