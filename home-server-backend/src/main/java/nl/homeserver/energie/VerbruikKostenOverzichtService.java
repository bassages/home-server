package nl.homeserver.energie;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.energie.StroomTariefIndicator.DAL;
import static nl.homeserver.energie.StroomTariefIndicator.NORMAAL;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.energiecontract.Energiecontract;
import nl.homeserver.energiecontract.EnergiecontractService;

@Service
public class VerbruikKostenOverzichtService {

    protected static final String CACHE_NAME_GAS_VERBRUIK_IN_PERIODE = "gasVerbruikInPeriode";
    protected static final String CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE = "stroomVerbruikInPeriode";

    @Autowired
    private VerbruikKostenOverzichtService verbruikKostenOverzichtServiceProxyWithEnabledCaching; // Needed to make use of use caching annotations

    private final EnergiecontractService energiecontractService;
    private final VerbruikRepository verbruikRepository;
    private final Clock clock;

    public VerbruikKostenOverzichtService(EnergiecontractService energiecontractService, VerbruikRepository verbruikRepository, Clock clock) {
        this.energiecontractService = energiecontractService;
        this.verbruikRepository = verbruikRepository;
        this.clock = clock;
    }

    public VerbruikKostenOverzicht getVerbruikEnKostenOverzicht(DateTimePeriod period) {
        VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();

        VerbruikKosten stroomVerbruikKostenDalTariefInPeriode = getStroomVerbruikInPeriode(period, DAL);
        verbruikKostenOverzicht.setStroomVerbruikDal(stroomVerbruikKostenDalTariefInPeriode.getVerbruik());
        verbruikKostenOverzicht.setStroomKostenDal(stroomVerbruikKostenDalTariefInPeriode.getKosten());

        VerbruikKosten stroomVerbruikKostenNormaalTariefInPeriode = getStroomVerbruikInPeriode(period, NORMAAL);
        verbruikKostenOverzicht.setStroomVerbruikNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getVerbruik());
        verbruikKostenOverzicht.setStroomKostenNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getKosten());

        VerbruikKosten gasVerbruikKostenInPeriode = getGasVerbruikInPeriode(period);
        verbruikKostenOverzicht.setGasVerbruik(gasVerbruikKostenInPeriode.getVerbruik());
        verbruikKostenOverzicht.setGasKosten(gasVerbruikKostenInPeriode.getKosten());

        return verbruikKostenOverzicht;
    }

    private VerbruikKosten getGasVerbruikInPeriode(DateTimePeriod period) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (period.startOnOrAfter(now)) {
            return VerbruikKosten.UNKNOWN;
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikKostenOverzichtServiceProxyWithEnabledCaching.getPotentiallyCachedGasVerbruikInPeriode(period);
        } else {
            return getNotCachedGasVerbruikInPeriode(period);
        }
    }

    private VerbruikKosten getStroomVerbruikInPeriode(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator) {
        LocalDateTime now = LocalDateTime.now(clock);

        if (period.startOnOrAfter(now)) {
            return VerbruikKosten.UNKNOWN;
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikKostenOverzichtServiceProxyWithEnabledCaching.getPotentiallyCachedStroomVerbruikInPeriode(period, stroomTariefIndicator);
        } else {
            return getNotCachedStroomVerbruikInPeriode(period, stroomTariefIndicator);
        }
    }

    @Cacheable(cacheNames = CACHE_NAME_GAS_VERBRUIK_IN_PERIODE)
    public VerbruikKosten getPotentiallyCachedGasVerbruikInPeriode(DateTimePeriod period) {
        return getNotCachedGasVerbruikInPeriode(period);
    }

    @Cacheable(cacheNames = CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE)
    public VerbruikKosten getPotentiallyCachedStroomVerbruikInPeriode(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator) {
        return getNotCachedStroomVerbruikInPeriode(period, stroomTariefIndicator);
    }

    private VerbruikKosten getNotCachedGasVerbruikInPeriode(DateTimePeriod period) {
        return energiecontractService.findAllInInPeriod(period)
                                     .stream()
                                     .map(energieContract -> this.getGasVerbruikKosten(energieContract, period))
                                     .collect(collectingAndThen(toList(), VerbruikenEnKosten::new))
                                     .sumToSingle();
    }

    private VerbruikKosten getNotCachedStroomVerbruikInPeriode(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator) {
        return energiecontractService.findAllInInPeriod(period)
                                     .stream()
                                     .map(energieContract -> this.getStroomVerbruikKosten(energieContract, stroomTariefIndicator, period))
                                     .collect(collectingAndThen(toList(), VerbruikenEnKosten::new))
                                     .sumToSingle();
    }

    private VerbruikKosten getGasVerbruikKosten(Energiecontract energiecontract, DateTimePeriod period) {
        DateTimePeriod subPeriod = getSubPeriod(energiecontract, period);

        BigDecimal verbruik = getGasVerbruik(subPeriod);
        BigDecimal kosten = null;

        if (verbruik != null) {
            kosten = energiecontract.getGasPerKuub().multiply(verbruik);
        }
        return new VerbruikKosten(verbruik, kosten);
    }

    private VerbruikKosten getStroomVerbruikKosten(Energiecontract energiecontract, StroomTariefIndicator stroomTariefIndicator, DateTimePeriod period) {
        DateTimePeriod subPeriod = getSubPeriod(energiecontract, period);

        BigDecimal verbruik = getStroomVerbruik(subPeriod, stroomTariefIndicator);
        BigDecimal kosten = null;

        if (verbruik != null) {
            kosten = energiecontract.getStroomKosten(stroomTariefIndicator).multiply(verbruik);
        }
        return new VerbruikKosten(verbruik, kosten);
    }

    private DateTimePeriod getSubPeriod(Energiecontract energiecontract, DateTimePeriod period) {
        LocalDateTime subFrom = Stream.of(period.getFromDateTime(), energiecontract.getValidFrom().atStartOfDay())
                                      .max(LocalDateTime::compareTo)
                                      .orElse(null);

        LocalDateTime subTo = Stream.of(period.getToDateTime(), energiecontract.getValidTo() != null ? energiecontract.getValidTo().atStartOfDay() : null)
                                    .filter(Objects::nonNull)
                                    .min(LocalDateTime::compareTo)
                                    .orElse(null);

        return aPeriodWithToDateTime(subFrom, subTo);
    }

    private BigDecimal getStroomVerbruik(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator) {
        if (stroomTariefIndicator == DAL) {
            return verbruikRepository.getStroomVerbruikDalTariefInPeriod(period.getFromDateTime(), period.getToDateTime());
        } else if (stroomTariefIndicator == NORMAAL) {
            return verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(period.getFromDateTime(), period.getToDateTime());
        } else {
            throw new IllegalArgumentException(format("Unexpected StroomTariefIndicator: [%s]", stroomTariefIndicator));
        }
    }

    private BigDecimal getGasVerbruik(DateTimePeriod period) {
        // Gas is registered once every hour, in the hour after it actually is used.
        // Compensate for that hour to make the query return the correct usages.
        return verbruikRepository.getGasVerbruikInPeriod(period.getFromDateTime().plusHours(1), period.getToDateTime().plusHours(1));
    }
}