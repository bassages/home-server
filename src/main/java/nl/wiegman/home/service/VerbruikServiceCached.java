package nl.wiegman.home.service;

import nl.wiegman.home.model.Energiecontract;
import nl.wiegman.home.model.Energiesoort;
import nl.wiegman.home.model.Verbruik;
import nl.wiegman.home.repository.EnergiecontractRepository;
import nl.wiegman.home.repository.MeterstandRepository;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class VerbruikServiceCached {

    private static final int KOSTEN_SCALE = 2;

    @Autowired
    MeterstandRepository meterstandRepository;

    @Autowired
    EnergiecontractRepository energiecontractRepository;

    @Cacheable(cacheNames = "stroomVerbruikInPeriode")
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
                return meterstandRepository.getGasVerbruikInPeriod(periodeVan, periodeTotEnMet);
            case STROOM:
                return meterstandRepository.getStroomVerbruikInPeriod(periodeVan, periodeTotEnMet);
            default:
                throw new UnsupportedOperationException("Unexpected energiesoort: " + energiesoort.name());
        }

    }
}
