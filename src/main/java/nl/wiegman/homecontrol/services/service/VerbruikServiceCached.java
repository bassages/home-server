package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.model.api.Verbruik;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import nl.wiegman.homecontrol.services.repository.MeterstandRepository;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class VerbruikServiceCached {

    @Inject
    MeterstandRepository meterstandRepository;

    @Inject
    KostenRepository kostenRepository;

    @Cacheable(cacheNames = "stroomVerbruikInPeriode")
    public Verbruik getPotentiallyCachedVerbruikInPeriode(Energiesoort energiesoort, long vanMillis, long totEnMetMillis) {
        return getVerbruikInPeriode(energiesoort, vanMillis, totEnMetMillis);
    }

    public Verbruik getVerbruikInPeriode(Energiesoort energiesoort, long periodeVan, long periodeTotEnMet) {
        BigDecimal totaalKosten = BigDecimal.ZERO;
        BigDecimal totaalVerbruik = null;

        if (periodeVan < System.currentTimeMillis()) {

            List<Kosten> kostenInPeriod = kostenRepository.getKostenInPeriod(periodeVan, periodeTotEnMet);

            if (CollectionUtils.isNotEmpty(kostenInPeriod)) {

                for (Kosten kosten : kostenInPeriod) {
                    long subVanMillis = kosten.getVan();
                    if (subVanMillis < periodeVan) {
                        subVanMillis = periodeVan;
                    }
                    long subTotEnMetMillis = kosten.getTotEnMet();
                    if (subTotEnMetMillis > periodeTotEnMet) {
                        subTotEnMetMillis = periodeTotEnMet;
                    }

                    BigDecimal verbruik = getVerbruik(energiesoort, subVanMillis, subTotEnMetMillis);

                    if (verbruik != null) {
                        if (totaalVerbruik == null) {
                            totaalVerbruik = BigDecimal.ZERO;
                        }
                        totaalKosten = totaalKosten.add(kosten.getKosten(energiesoort).multiply(verbruik));
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
            verbruik.setKosten(totaalKosten.setScale(2, RoundingMode.CEILING));
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
