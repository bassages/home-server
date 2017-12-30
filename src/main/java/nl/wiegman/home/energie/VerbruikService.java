package nl.wiegman.home.energie;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.time.Month.JANUARY;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static nl.wiegman.home.DateTimePeriod.aPeriodWithEndDateTime;
import static nl.wiegman.home.DateTimePeriod.aPeriodWithToDateTime;
import static nl.wiegman.home.DateTimeUtil.getAllMonths;
import static nl.wiegman.home.DateTimeUtil.getDaysInPeriod;
import static nl.wiegman.home.DateTimeUtil.toLocalDateTime;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpoch;
import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_HOUR;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DatePeriod;
import nl.wiegman.home.DateTimePeriod;
import nl.wiegman.home.energiecontract.Energiecontract;
import nl.wiegman.home.energiecontract.EnergiecontractService;

@Service
public class VerbruikService {

    protected static final String CACHE_NAME_GAS_VERBRUIK_IN_PERIODE = "gasVerbruikInPeriode";
    protected static final String CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE = "stroomVerbruikInPeriode";

    @Autowired
    private VerbruikService verbruikServiceProxyWithEnabledCaching; // Needed to make use of use caching annotations

    private final MeterstandService meterstandService;
    private final EnergiecontractService energiecontractService;
    private final VerbruikRepository verbruikRepository;
    private final Clock clock;

    @Autowired
    public VerbruikService(MeterstandService meterstandService, EnergiecontractService energiecontractService, VerbruikRepository verbruikRepository, Clock clock) {
        this.meterstandService = meterstandService;
        this.energiecontractService = energiecontractService;
        this.verbruikRepository = verbruikRepository;
        this.clock = clock;
    }

    public List<VerbruikInUurOpDag> getVerbruikPerUurOpDag(LocalDate day) {
        return IntStream.rangeClosed(0, 23)
                        .mapToObj(hourOfDay -> getVerbruikInUur(day, hourOfDay))
                        .collect(toList());
    }

    public List<VerbruikInMaandInJaar> getVerbruikPerMaandInJaar(Year year) {
        return getAllMonths().stream()
                             .map(monthInYear -> getVerbruikInMaand(YearMonth.of(year.getValue(), monthInYear)))
                             .collect(toList());
    }

    public List<VerbruikKostenOpDag> getVerbruikPerDag(DatePeriod period) {
        return getDaysInPeriod(period).stream()
                                      .map(this::getVerbruikOpDag)
                                      .collect(toList());
    }

    public List<VerbruikInJaar> getVerbruikPerJaar() {
        Meterstand oldest = meterstandService.getOldest();
        Meterstand mostRecent = meterstandService.getMostRecent();

        if (oldest == null) {
            return emptyList();
        }

        int from = oldest.getDatumtijdAsLocalDateTime().getYear();
        int to = mostRecent.getDatumtijdAsLocalDateTime().plusYears(1).getYear();

        return IntStream.range(from, to)
                        .mapToObj(year -> getVerbruikInJaar(Year.of(year)))
                        .collect(toList());
    }

    public VerbruikKostenOverzicht getGemiddeldeVerbruikPerDagInPeriode(DatePeriod period) {
        List<VerbruikKostenOpDag> verbruikPerDag = getVerbruikPerDag(period);

        VerbruikKostenOverzicht verbruikKostenOverzicht = new VerbruikKostenOverzicht();
        verbruikKostenOverzicht.setStroomVerbruikDal(berekenGemiddelde(verbruikPerDag, VerbruikKostenOverzicht::getStroomVerbruikDal, 3));
        verbruikKostenOverzicht.setStroomVerbruikNormaal(berekenGemiddelde(verbruikPerDag, VerbruikKostenOverzicht::getStroomVerbruikNormaal, 3));
        verbruikKostenOverzicht.setGasVerbruik(berekenGemiddelde(verbruikPerDag, VerbruikKostenOverzicht::getGasVerbruik, 3));
        verbruikKostenOverzicht.setStroomKostenDal(berekenGemiddelde(verbruikPerDag, VerbruikKostenOverzicht::getStroomKostenDal, 2));
        verbruikKostenOverzicht.setStroomKostenNormaal(berekenGemiddelde(verbruikPerDag, VerbruikKostenOverzicht::getStroomKostenNormaal, 2));
        verbruikKostenOverzicht.setGasKosten(berekenGemiddelde(verbruikPerDag, VerbruikKostenOverzicht::getGasKosten, 2));
        return verbruikKostenOverzicht;
    }

    private BigDecimal berekenGemiddelde(List<VerbruikKostenOpDag> verbruikPerDag, Function<VerbruikKostenOverzicht, BigDecimal> attributeToAverageGetter, int scale) {
        BigDecimal sumVerbruik = verbruikPerDag.stream()
                                               .map(VerbruikKostenOpDag::getVerbruikKostenOverzicht)
                                               .map(attributeToAverageGetter)
                                               .filter(Objects::nonNull)
                                               .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumVerbruik.divide(new BigDecimal(verbruikPerDag.size()), ROUND_HALF_UP).setScale(scale, ROUND_HALF_UP);
    }

    private VerbruikInJaar getVerbruikInJaar(Year year) {
        LocalDateTime from = LocalDate.of(year.getValue(), JANUARY, 1).atStartOfDay();
        LocalDateTime to = from.plusYears(1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);
        return new VerbruikInJaar(year.getValue(), getVerbruikEnKostenOverzicht(period));
    }

    private VerbruikInUurOpDag getVerbruikInUur(LocalDate day, int hour) {
        LocalDateTime from = day.atStartOfDay().plusHours(hour);
        LocalDateTime to = day.atStartOfDay().plusHours(hour + 1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);
        return new VerbruikInUurOpDag(hour, getVerbruikEnKostenOverzicht(period));
    }

    private VerbruikInMaandInJaar getVerbruikInMaand(YearMonth yearMonth) {
        LocalDateTime from = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime to = yearMonth.atDay(1).atStartOfDay().plusMonths(1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);
        return new VerbruikInMaandInJaar(yearMonth.getMonthValue(), getVerbruikEnKostenOverzicht(period));
    }

    private VerbruikKostenOpDag getVerbruikOpDag(LocalDate day) {
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.atStartOfDay().plusDays(1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);
        return new VerbruikKostenOpDag(day, getVerbruikEnKostenOverzicht(period));
    }

    private VerbruikKostenOverzicht getVerbruikEnKostenOverzicht(DateTimePeriod period) {
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
            return verbruikServiceProxyWithEnabledCaching.getPotentiallyCachedGasVerbruikInPeriode(period);
        } else {
            return getNotCachedGasVerbruikInPeriode(period);
        }
    }

    private VerbruikKosten getStroomVerbruikInPeriode(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (period.getStartDateTime().isAfter(now) || period.getStartDateTime().isEqual(now)) {
            return VerbruikKosten.UNKNOWN;
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikServiceProxyWithEnabledCaching.getPotentiallyCachedStroomVerbruikInPeriode(period, stroomTariefIndicator);
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