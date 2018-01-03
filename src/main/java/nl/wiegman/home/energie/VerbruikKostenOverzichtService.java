package nl.wiegman.home.energie;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static nl.wiegman.home.DateTimePeriod.aPeriodWithEndDateTime;
import static nl.wiegman.home.DateTimeUtil.toLocalDateTime;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpoch;
import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_HOUR;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DateTimePeriod;
import nl.wiegman.home.energiecontract.Energiecontract;
import nl.wiegman.home.energiecontract.EnergiecontractService;

@Service
public class VerbruikKostenOverzichtService {

    protected static final String CACHE_NAME_GAS_VERBRUIK_IN_PERIODE = "gasVerbruikInPeriode";
    protected static final String CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE = "stroomVerbruikInPeriode";

    @Autowired
    private VerbruikKostenOverzichtService verbruikKostenOverzichtServiceProxyWithEnabledCaching; // Needed to make use of use caching annotations

    private final EnergiecontractService energiecontractService;
    private final VerbruikRepository verbruikRepository;
    private final Clock clock;

    @Autowired
    public VerbruikKostenOverzichtService(EnergiecontractService energiecontractService, VerbruikRepository verbruikRepository, Clock clock) {
        this.energiecontractService = energiecontractService;
        this.verbruikRepository = verbruikRepository;
        this.clock = clock;
    }

    public VerbruikKostenOverzicht getVerbruikEnKostenOverzicht(DateTimePeriod period) {
        VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();

        VerbruikKosten gasVerbruikKostenInPeriode = getGasVerbruikInPeriode(period);
        verbruikKostenOverzicht.setGasVerbruik(gasVerbruikKostenInPeriode.getVerbruik());
        verbruikKostenOverzicht.setGasKosten(gasVerbruikKostenInPeriode.getKosten());

        VerbruikKosten stroomVerbruikKostenDalTariefInPeriode = getStroomVerbruikInPeriode(period, StroomTariefIndicator.DAL);
        verbruikKostenOverzicht.setStroomVerbruikDal(stroomVerbruikKostenDalTariefInPeriode.getVerbruik());
        verbruikKostenOverzicht.setStroomKostenDal(stroomVerbruikKostenDalTariefInPeriode.getKosten());

        VerbruikKosten stroomVerbruikKostenNormaalTariefInPeriode = getStroomVerbruikInPeriode(period, StroomTariefIndicator.NORMAAL);
        verbruikKostenOverzicht.setStroomVerbruikNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getVerbruik());
        verbruikKostenOverzicht.setStroomKostenNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getKosten());

        return verbruikKostenOverzicht;
    }

    private VerbruikKosten getGasVerbruikInPeriode(DateTimePeriod period) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (period.getStartDateTime().isAfter(now) || period.getStartDateTime().isEqual(now)) {
            return VerbruikKosten.UNKNOWN;
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikKostenOverzichtServiceProxyWithEnabledCaching.getPotentiallyCachedGasVerbruikInPeriode(period);
        } else {
            return getNotCachedGasVerbruikInPeriode(period);
        }
    }

    private VerbruikKosten getStroomVerbruikInPeriode(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (period.getStartDateTime().isAfter(now) || period.getStartDateTime().isEqual(now)) {
            return VerbruikKosten.UNKNOWN;
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikKostenOverzichtServiceProxyWithEnabledCaching.getPotentiallyCachedStroomVerbruikInPeriode(period, stroomTariefIndicator);
        } else {
            return getNotCachedStroomVerbruikInPeriode(period, stroomTariefIndicator);
        }
    }

    public VerbruikKostenOverzicht getAverages(List<VerbruikKostenOverzicht> verbruikKostenOverzichten) {
        VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht.setStroomVerbruikDal(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomVerbruikDal, 3));
        verbruikKostenOverzicht.setStroomVerbruikNormaal(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomVerbruikNormaal, 3));
        verbruikKostenOverzicht.setGasVerbruik(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getGasVerbruik, 3));
        verbruikKostenOverzicht.setStroomKostenDal(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomKostenDal, 2));
        verbruikKostenOverzicht.setStroomKostenNormaal(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getStroomKostenNormaal, 2));
        verbruikKostenOverzicht.setGasKosten(berekenGemiddelde(verbruikKostenOverzichten, VerbruikKostenOverzicht::getGasKosten, 2));
        return verbruikKostenOverzicht;
    }

    private BigDecimal berekenGemiddelde(List<VerbruikKostenOverzicht> verbruikKostenOverzichtPerDag, Function<VerbruikKostenOverzicht, BigDecimal> attributeToAverageGetter, int scale) {
        BigDecimal sumVerbruik = verbruikKostenOverzichtPerDag.stream()
                                                              .map(attributeToAverageGetter)
                                                              .filter(Objects::nonNull)
                                                             .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumVerbruik.divide(new BigDecimal(verbruikKostenOverzichtPerDag.size()), ROUND_HALF_UP).setScale(scale, ROUND_HALF_UP);
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
                                     .collect(collectingAndThen(toList(), Verbruiken::new))
                                     .sumToSingle();
    }

    private VerbruikKosten getNotCachedStroomVerbruikInPeriode(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator) {
        return energiecontractService.findAllInInPeriod(period)
                                     .stream()
                                     .map(energieContract -> this.getStroomVerbruikKosten(energieContract, stroomTariefIndicator, period))
                                     .collect(collectingAndThen(toList(), Verbruiken::new))
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
        long periodeVan = toMillisSinceEpoch(period.getStartDateTime());
        long periodeTotEnMet = toMillisSinceEpoch(period.getEndDateTime());

        long subVanMillis = energiecontract.getVan();
        long subTotEnMetMillis = energiecontract.getTotEnMet();

        if (subVanMillis < periodeVan) {
            subVanMillis = periodeVan;
        }
        if (subTotEnMetMillis > periodeTotEnMet) {
            subTotEnMetMillis = periodeTotEnMet;
        }
        return aPeriodWithEndDateTime(toLocalDateTime(subVanMillis), toLocalDateTime(subTotEnMetMillis));
    }

    private BigDecimal getStroomVerbruik(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator) {
        long periodeVan = toMillisSinceEpoch(period.getStartDateTime());
        long periodeTotEnMet = toMillisSinceEpoch(period.getEndDateTime());

        switch (stroomTariefIndicator) {
            case DAL:
                return verbruikRepository.getStroomVerbruikDalTariefInPeriod(periodeVan, periodeTotEnMet);
            case NORMAAL:
                return verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(periodeVan, periodeTotEnMet);
            default:
                throw new IllegalArgumentException("Unexpected StroomTariefIndicator: " + stroomTariefIndicator.name());
        }
    }

    private BigDecimal getGasVerbruik(DateTimePeriod period) {
        // Gas is registered once every hour, in the hour after it actually is used.
        // Compensate for that hour to make the query return the correct usages.
        long periodeVan = toMillisSinceEpoch(period.getStartDateTime()) + MILLIS_PER_HOUR;
        long periodeTotEnMet = toMillisSinceEpoch(period.getEndDateTime()) + MILLIS_PER_HOUR;
        return verbruikRepository.getGasVerbruikInPeriod(periodeVan, periodeTotEnMet);
    }
}