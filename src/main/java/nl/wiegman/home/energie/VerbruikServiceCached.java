package nl.wiegman.home.energie;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import nl.wiegman.home.energiecontract.Energiecontract;
import nl.wiegman.home.energiecontract.EnergiecontractRepository;

@Service
public class VerbruikServiceCached {

    public static final String CACHE_NAME_GAS_VERBRUIK_IN_PERIODE = "gasVerbruikInPeriode";
    public static final String CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE = "stroomVerbruikInPeriode";

    private static final int KOSTEN_SCALE = 3;

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
        BigDecimal totaalKosten = BigDecimal.ZERO;
        BigDecimal totaalVerbruik = null;

        if (periodeVan < System.currentTimeMillis()) {

            List<Energiecontract> energiecontractInPeriod = energiecontractRepository.findAllInInPeriod(periodeVan, periodeTotEnMet);

            if (CollectionUtils.isNotEmpty(energiecontractInPeriod)) {

                for (Energiecontract energiecontract : energiecontractInPeriod) {
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
                        if (totaalVerbruik == null) {
                            totaalVerbruik = BigDecimal.ZERO;
                        }
                        totaalKosten = totaalKosten.add(energiecontract.getGasPerKuub().multiply(verbruik));
                        totaalVerbruik = totaalVerbruik.add(verbruik);
                    }
                }
            } else {
                BigDecimal verbruik = getGasVerbruik(periodeVan, periodeTotEnMet);
                if (verbruik != null) {
                    totaalVerbruik = verbruik;
                }
            }
        }

        Verbruik gasVerbruik = new Verbruik();
        gasVerbruik.setVerbruik(totaalVerbruik);
        if (totaalVerbruik == null) {
            gasVerbruik.setKosten(null);
        } else {
            gasVerbruik.setKosten(totaalKosten.setScale(KOSTEN_SCALE, RoundingMode.CEILING));
        }
        return gasVerbruik;
    }

    public Verbruik getStroomVerbruikInPeriode(long periodeVan, long periodeTotEnMet, StroomTariefIndicator stroomTariefIndicator) {
        BigDecimal totaalKosten = BigDecimal.ZERO;
        BigDecimal totaalVerbruik = null;

        if (periodeVan < System.currentTimeMillis()) {

            List<Energiecontract> energiecontractInPeriod = energiecontractRepository.findAllInInPeriod(periodeVan, periodeTotEnMet);

            if (CollectionUtils.isNotEmpty(energiecontractInPeriod)) {

                for (Energiecontract energiecontract : energiecontractInPeriod) {
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
                        if (totaalVerbruik == null) {
                            totaalVerbruik = BigDecimal.ZERO;
                        }
                        totaalKosten = totaalKosten.add(energiecontract.getStroomKosten(stroomTariefIndicator).multiply(verbruik));
                        totaalVerbruik = totaalVerbruik.add(verbruik);
                    }
                }
            } else {
                BigDecimal verbruik = getStroomVerbruik(periodeVan, periodeTotEnMet, stroomTariefIndicator);
                if (verbruik != null) {
                    totaalVerbruik = verbruik;
                }
            }
        }

        Verbruik stroomVerbruik = new Verbruik();
        stroomVerbruik.setVerbruik(totaalVerbruik);
        if (totaalVerbruik == null) {
            stroomVerbruik.setKosten(null);
        } else {
            stroomVerbruik.setKosten(totaalKosten.setScale(KOSTEN_SCALE, RoundingMode.CEILING));
        }
        return stroomVerbruik;
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
        return meterstandRepository.getGasVerbruikInPeriod(periodeVan + DateUtils.MILLIS_PER_HOUR, periodeTotEnMet + DateUtils.MILLIS_PER_HOUR);
    }
}
