package nl.wiegman.home.energie;

import nl.wiegman.home.energiecontract.Energiecontract;
import nl.wiegman.home.energiecontract.EnergiecontractRepository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class VerbruikServiceCached {

    private static final int KOSTEN_SCALE = 3;

    private final MeterstandRepository meterstandRepository;
    private final EnergiecontractRepository energiecontractRepository;

    @Autowired
    public VerbruikServiceCached(MeterstandRepository meterstandRepository, EnergiecontractRepository energiecontractRepository) {
        this.meterstandRepository = meterstandRepository;
        this.energiecontractRepository = energiecontractRepository;
    }

    @Cacheable(cacheNames = "energieVerbruikInPeriode")
    public Verbruik getPotentiallyCachedVerbruikInPeriode(Energiesoort energiesoort, long vanMillis, long totEnMetMillis) {
        return getVerbruikInPeriode(energiesoort, vanMillis, totEnMetMillis);
    }

    public Verbruik getVerbruikInPeriode(Energiesoort energiesoort, long periodeVan, long periodeTotEnMet) {
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

                    BigDecimal verbruik = getVerbruik(energiesoort, subVanMillis, subTotEnMetMillis);

                    if (verbruik != null) {
                        if (totaalVerbruik == null) {
                            totaalVerbruik = BigDecimal.ZERO;
                        }
                        totaalKosten = totaalKosten.add(energiecontract.getKosten(energiesoort).multiply(verbruik));
                        totaalVerbruik = totaalVerbruik.add(verbruik);
                    }
                }
            } else {
                BigDecimal verbruik = getVerbruik(energiesoort, periodeVan, periodeTotEnMet);
                if (verbruik != null) {
                    totaalVerbruik = verbruik;
                }
            }
        }

        Verbruik verbruik = new Verbruik();
        verbruik.setVerbruik(totaalVerbruik);
        if (totaalVerbruik == null) {
            verbruik.setKosten(null);
        } else {
            verbruik.setKosten(totaalKosten.setScale(KOSTEN_SCALE, RoundingMode.CEILING));
        }
        return verbruik;
    }

    private BigDecimal getVerbruik(Energiesoort energiesoort, long periodeVan, long periodeTotEnMet) {
        switch (energiesoort) {
            case GAS:
                // Gas is registered once every hour, in the hour after it actually is used.
                // Compensate for that hour to make the query return the correct usages.
                return meterstandRepository.getGasVerbruikInPeriod(periodeVan + DateUtils.MILLIS_PER_HOUR, periodeTotEnMet + DateUtils.MILLIS_PER_HOUR);
            case STROOM:
                BigDecimal stroomVerbruikNormaalTariefInPeriod = meterstandRepository.getStroomVerbruikNormaalTariefInPeriod(periodeVan, periodeTotEnMet);
                BigDecimal stroomVerbruikLaagTariefInPeriod = meterstandRepository.getStroomVerbruikLaagTariefInPeriod(periodeVan, periodeTotEnMet);

                return nullSafeAdd(stroomVerbruikNormaalTariefInPeriod, stroomVerbruikLaagTariefInPeriod);
            default:
                throw new UnsupportedOperationException("Unexpected energiesoort: " + energiesoort.name());
        }
    }

    private BigDecimal nullSafeAdd(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        BigDecimal result = null;

        if (bigDecimal1 != null && bigDecimal2 != null) {
            result = bigDecimal1.add(bigDecimal2);
        } else if (bigDecimal2 != null) {
            result = bigDecimal2;
        } else if (bigDecimal1 != null) {
            result = bigDecimal1;
        }

        return result;
    }
}
