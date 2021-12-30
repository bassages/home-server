package nl.homeserver.energie.verbruikkosten;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import nl.homeserver.energie.energycontract.EnergyContractService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class VerbruikKostenInPeriodService {

    public static final String CACHE_NAME_GAS_VERBRUIK_IN_PERIODE = "gasVerbruikInPeriode";
    public static final String CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE = "stroomVerbruikInPeriode";

    private final EnergyContractService energyContractService;
    private final VerbruikKostenService verbruikKostenService;

    @SuppressWarnings("WeakerAccess")
    @Cacheable(cacheNames = CACHE_NAME_GAS_VERBRUIK_IN_PERIODE, key = "#verbruikProvider.getClass().getName() + '-' + #period")
    public VerbruikKosten getPotentiallyCachedGasVerbruikInPeriode(final VerbruikProvider verbruikProvider, final DateTimePeriod period) {
        return getNotCachedGasVerbruikInPeriode(verbruikProvider, period);
    }

    public VerbruikKosten getNotCachedGasVerbruikInPeriode(final VerbruikProvider verbruikProvider,
                                                           final DateTimePeriod period) {
        return energyContractService.findAllInInPeriod(period)
                .stream()
                .map(energyContract -> verbruikKostenService.getGasVerbruikKosten(verbruikProvider, energyContract, period))
                .collect(collectingAndThen(toList(), VerbruikenEnKosten::new))
                .sumToSingle();
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable(cacheNames = CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE, key = "#verbruikProvider.getClass().getName() + '-' + #period + '-' + #stroomTariefIndicator")
    public VerbruikKosten getPotentiallyCachedStroomVerbruikInPeriode(final VerbruikProvider verbruikProvider,
                                                                      final DateTimePeriod period,
                                                                      final StroomTariefIndicator stroomTariefIndicator) {
        return getNotCachedStroomVerbruikInPeriode(verbruikProvider, period, stroomTariefIndicator);
    }

    public VerbruikKosten getNotCachedStroomVerbruikInPeriode(final VerbruikProvider verbruikProvider,
                                                              final DateTimePeriod period,
                                                              final StroomTariefIndicator stroomTariefIndicator) {
        return energyContractService.findAllInInPeriod(period)
                .stream()
                .map(energyContract -> verbruikKostenService.getStroomVerbruikKosten(verbruikProvider, energyContract, stroomTariefIndicator, period))
                .collect(collectingAndThen(toList(), VerbruikenEnKosten::new))
                .sumToSingle();
    }
}
