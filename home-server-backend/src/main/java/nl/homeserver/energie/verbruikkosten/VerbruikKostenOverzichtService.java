package nl.homeserver.energie.verbruikkosten;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.energie.StroomTariefIndicator.DAL;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;
import static org.apache.commons.lang3.ObjectUtils.max;
import static org.apache.commons.lang3.ObjectUtils.min;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.StroomTariefIndicator;
import nl.homeserver.energie.energiecontract.Energiecontract;
import nl.homeserver.energie.energiecontract.EnergiecontractService;

@Service
public class VerbruikKostenOverzichtService {

    public static final String CACHE_NAME_GAS_VERBRUIK_IN_PERIODE = "gasVerbruikInPeriode";
    public static final String CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE = "stroomVerbruikInPeriode";

    // Needed to make use of use caching annotations
    @Autowired
    private VerbruikKostenOverzichtService verbruikKostenOverzichtServiceProxyWithEnabledCaching;

    private final EnergiecontractService energiecontractService;
    private final Clock clock;

    VerbruikKostenOverzichtService(final EnergiecontractService energiecontractService,
                                   final Clock clock) {
        this.energiecontractService = energiecontractService;
        this.clock = clock;
    }

    public VerbruikKostenOverzicht getVerbruikEnKostenOverzicht(final VerbruikProvider verbruikProvider,
                                                                final DateTimePeriod period) {
        final VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();

        final VerbruikKosten stroomVerbruikKostenDalTariefInPeriode = getStroomVerbruikInPeriode(verbruikProvider, period, DAL);
        verbruikKostenOverzicht.setStroomVerbruikDal(stroomVerbruikKostenDalTariefInPeriode.getVerbruik());
        verbruikKostenOverzicht.setStroomKostenDal(stroomVerbruikKostenDalTariefInPeriode.getKosten());

        final VerbruikKosten stroomVerbruikKostenNormaalTariefInPeriode = getStroomVerbruikInPeriode(verbruikProvider, period, NORMAAL);
        verbruikKostenOverzicht.setStroomVerbruikNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getVerbruik());
        verbruikKostenOverzicht.setStroomKostenNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getKosten());

        final VerbruikKosten gasVerbruikKostenInPeriode = getGasVerbruikInPeriode(verbruikProvider, period);
        verbruikKostenOverzicht.setGasVerbruik(gasVerbruikKostenInPeriode.getVerbruik());
        verbruikKostenOverzicht.setGasKosten(gasVerbruikKostenInPeriode.getKosten());

        return verbruikKostenOverzicht;
    }

    private VerbruikKosten getGasVerbruikInPeriode(final VerbruikProvider verbruikProvider, final DateTimePeriod period) {
        final LocalDateTime now = now(clock);
        if (period.startsOnOrAfter(now)) {
            return VerbruikKosten.UNKNOWN;
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikKostenOverzichtServiceProxyWithEnabledCaching.getPotentiallyCachedGasVerbruikInPeriode(verbruikProvider, period);
        } else {
            return getNotCachedGasVerbruikInPeriode(verbruikProvider, period);
        }
    }

    private VerbruikKosten getStroomVerbruikInPeriode(final VerbruikProvider verbruikProvider,
                                                      final DateTimePeriod period,
                                                      final StroomTariefIndicator stroomTariefIndicator) {
        final LocalDateTime now = now(clock);

        if (period.startsOnOrAfter(now)) {
            return VerbruikKosten.UNKNOWN;
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikKostenOverzichtServiceProxyWithEnabledCaching.getPotentiallyCachedStroomVerbruikInPeriode(verbruikProvider, period, stroomTariefIndicator);
        } else {
            return getNotCachedStroomVerbruikInPeriode(verbruikProvider, period, stroomTariefIndicator);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable(cacheNames = CACHE_NAME_GAS_VERBRUIK_IN_PERIODE, key = "#verbruikProvider.getClass().getName() + '-' + #period")
    public VerbruikKosten getPotentiallyCachedGasVerbruikInPeriode(final VerbruikProvider verbruikProvider, final DateTimePeriod period) {
        return getNotCachedGasVerbruikInPeriode(verbruikProvider, period);
    }

    @SuppressWarnings("WeakerAccess")
    @Cacheable(cacheNames = CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE, key = "#verbruikProvider.getClass().getName() + '-' + #period + '-' + #stroomTariefIndicator")
    public VerbruikKosten getPotentiallyCachedStroomVerbruikInPeriode(final VerbruikProvider verbruikProvider,
                                                                      final DateTimePeriod period,
                                                                      final StroomTariefIndicator stroomTariefIndicator) {
        return getNotCachedStroomVerbruikInPeriode(verbruikProvider, period, stroomTariefIndicator);
    }

    private VerbruikKosten getNotCachedGasVerbruikInPeriode(final VerbruikProvider verbruikProvider,
                                                            final DateTimePeriod period) {
        return energiecontractService.findAllInInPeriod(period)
                                     .stream()
                                     .map(energieContract -> this.getGasVerbruikKosten(verbruikProvider, energieContract, period))
                                     .collect(collectingAndThen(toList(), VerbruikenEnKosten::new))
                                     .sumToSingle();
    }

    private VerbruikKosten getNotCachedStroomVerbruikInPeriode(final VerbruikProvider verbruikProvider,
                                                               final DateTimePeriod period,
                                                               final StroomTariefIndicator stroomTariefIndicator) {
        return energiecontractService.findAllInInPeriod(period)
                                     .stream()
                                     .map(energieContract -> this.getStroomVerbruikKosten(verbruikProvider, energieContract, stroomTariefIndicator, period))
                                     .collect(collectingAndThen(toList(), VerbruikenEnKosten::new))
                                     .sumToSingle();
    }

    private VerbruikKosten getGasVerbruikKosten(final VerbruikProvider verbruikProvider,
                                                final Energiecontract energiecontract,
                                                final DateTimePeriod period) {
        final DateTimePeriod subPeriod = getSubPeriod(energiecontract, period);

        final BigDecimal verbruik = verbruikProvider.getGasVerbruik(subPeriod);

        BigDecimal kosten = null;
        if (verbruik != null) {
            kosten = energiecontract.getGasPerKuub().multiply(verbruik);
        }
        return new VerbruikKosten(verbruik, kosten);
    }

    private VerbruikKosten getStroomVerbruikKosten(final VerbruikProvider verbruikProvider,
                                                   final Energiecontract energiecontract,
                                                   final StroomTariefIndicator stroomTariefIndicator,
                                                   final DateTimePeriod period) {
        final DateTimePeriod subPeriod = getSubPeriod(energiecontract, period);

        final BigDecimal verbruik = verbruikProvider.getStroomVerbruik(subPeriod, stroomTariefIndicator);

        BigDecimal kosten = null;
        if (verbruik != null) {
            kosten = energiecontract.getStroomKosten(stroomTariefIndicator).multiply(verbruik);
        }
        return new VerbruikKosten(verbruik, kosten);
    }

    private DateTimePeriod getSubPeriod(final Energiecontract energiecontract, final DateTimePeriod period) {
        final LocalDateTime subFrom = max(period.getFromDateTime(), energiecontract.getValidFrom().atStartOfDay());
        final LocalDateTime subTo = min(period.getToDateTime(), energiecontract.getValidTo() != null ? energiecontract.getValidTo().atStartOfDay() : null);
        return aPeriodWithToDateTime(subFrom, subTo);
    }
}