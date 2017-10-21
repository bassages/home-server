package nl.wiegman.home.energie;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_HOUR;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import nl.wiegman.home.energiecontract.Energiecontract;
import nl.wiegman.home.energiecontract.EnergiecontractRepository;

@Service
public class VerbruikServiceCached {

    public static final String CACHE_NAME_GAS_VERBRUIK_IN_PERIODE = "gasVerbruikInPeriode";
    public static final String CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE = "stroomVerbruikInPeriode";

    private final MeterstandRepository meterstandRepository;
    private final EnergiecontractRepository energiecontractRepository;

    @Autowired
    public VerbruikServiceCached(MeterstandRepository meterstandRepository, EnergiecontractRepository energiecontractRepository) {
        this.meterstandRepository = meterstandRepository;
        this.energiecontractRepository = energiecontractRepository;
    }

    @Cacheable(cacheNames = CACHE_NAME_GAS_VERBRUIK_IN_PERIODE)
    public Verbruik getPotentiallyCachedGasVerbruikInPeriode(long vanMillis, long totEnMetMillis) {
        return getGasVerbruikInPeriode(vanMillis, totEnMetMillis);
    }

    @Cacheable(cacheNames = CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE)
    public Verbruik getPotentiallyCachedStroomVerbruikInPeriode(long vanMillis, long totEnMetMillis, StroomTariefIndicator stroomTariefIndicator) {
        return getStroomVerbruikInPeriode(vanMillis, totEnMetMillis, stroomTariefIndicator);
    }

    public Verbruik getGasVerbruikInPeriode(long periodeVan, long periodeTotEnMet) {
        Verbruik gasVerbruik = new Verbruik();

        List<Energiecontract> energiecontractsInPeriod = energiecontractRepository.findAllInInPeriod(periodeVan, periodeTotEnMet);

        if (isNotEmpty(energiecontractsInPeriod)) {
            for (Energiecontract energiecontract : energiecontractsInPeriod) {
                addGasVerbruik(gasVerbruik, energiecontract, periodeVan, periodeTotEnMet);
            }
        } else {
            BigDecimal verbruik = getGasVerbruik(periodeVan, periodeTotEnMet);
            if (verbruik != null) {
                gasVerbruik.addVerbruik(verbruik);
            }
        }
        return gasVerbruik;
    }

    private void addGasVerbruik(Verbruik gasVerbruik, Energiecontract energiecontract, long periodeVan, long periodeTotEnMet) {
        long subVanMillis = energiecontract.getVan();
        if (subVanMillis < periodeVan) {
            subVanMillis = periodeVan;
        }
        long subTotEnMetMillis = energiecontract.getTotEnMet();
        if (subTotEnMetMillis > periodeTotEnMet) {
            subTotEnMetMillis = periodeTotEnMet;
        }

        BigDecimal verbruik = getGasVerbruik(subVanMillis, subTotEnMetMillis);
        if (verbruik != null) {
            gasVerbruik.addVerbruik(verbruik);
            gasVerbruik.addKosten(energiecontract.getGasPerKuub().multiply(verbruik));
        }
    }

    public Verbruik getStroomVerbruikInPeriode(long periodeVan, long periodeTotEnMet, StroomTariefIndicator stroomTariefIndicator) {
        Verbruik stroomVerbruik = new Verbruik();

        List<Energiecontract> energiecontractInPeriod = energiecontractRepository.findAllInInPeriod(periodeVan, periodeTotEnMet);

        if (isNotEmpty(energiecontractInPeriod)) {
            for (Energiecontract energiecontract : energiecontractInPeriod) {
                addStroomVerbruik(stroomVerbruik, energiecontract, stroomTariefIndicator, periodeVan, periodeTotEnMet);
            }
        } else {
            BigDecimal verbruik = getStroomVerbruik(periodeVan, periodeTotEnMet, stroomTariefIndicator);
            if (verbruik != null) {
                stroomVerbruik.addVerbruik(verbruik);
            }
        }

        return stroomVerbruik;
    }

    private void addStroomVerbruik(Verbruik stroomVerbruik, Energiecontract energiecontract, StroomTariefIndicator stroomTariefIndicator,
            long periodeVan, long periodeTotEnMet) {

        long subVanMillis = energiecontract.getVan();
        if (subVanMillis < periodeVan) {
            subVanMillis = periodeVan;
        }
        long subTotEnMetMillis = energiecontract.getTotEnMet();
        if (subTotEnMetMillis > periodeTotEnMet) {
            subTotEnMetMillis = periodeTotEnMet;
        }

        BigDecimal verbruik = getStroomVerbruik(subVanMillis, subTotEnMetMillis, stroomTariefIndicator);

        if (verbruik != null) {
            stroomVerbruik.addVerbruik(verbruik);
            stroomVerbruik.addKosten(energiecontract.getStroomKosten(stroomTariefIndicator).multiply(verbruik));
        }
    }

    private BigDecimal getStroomVerbruik(long periodeVan, long periodeTotEnMet, StroomTariefIndicator stroomTariefIndicator) {
        switch (stroomTariefIndicator) {
            case DAL:
                return meterstandRepository.getStroomVerbruikDalTariefInPeriod(periodeVan, periodeTotEnMet);
            case NORMAAL:
                return  meterstandRepository.getStroomVerbruikNormaalTariefInPeriod(periodeVan, periodeTotEnMet);
            default:
                throw new UnsupportedOperationException("Unexpected StroomTariefIndicator: " + stroomTariefIndicator.name());
        }
    }

    private BigDecimal getGasVerbruik(long periodeVan, long periodeTotEnMet) {
        // Gas is registered once every hour, in the hour after it actually is used.
        // Compensate for that hour to make the query return the correct usages.
        return meterstandRepository.getGasVerbruikInPeriod(periodeVan + MILLIS_PER_HOUR, periodeTotEnMet + MILLIS_PER_HOUR);
    }
}
