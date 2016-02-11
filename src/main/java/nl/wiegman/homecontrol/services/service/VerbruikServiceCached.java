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
    public Verbruik getPotentiallyCachedVerbruikInPeriode(long vanMillis, long totEnMetMillis) {
        return getVerbruikInPeriode(vanMillis, totEnMetMillis);
    }

    public Verbruik getVerbruikInPeriode(long periodeVan, long periodeTotEnMet) {
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

                    BigDecimal verbruik = meterstandRepository.getStroomVerbruikInPeriod(subVanMillis, subTotEnMetMillis);
                    if (verbruik != null) {
                        if (totaalVerbruik == null) {
                            totaalVerbruik = BigDecimal.ZERO;
                        }
                        totaalKosten = totaalKosten.add(kosten.getStroomPerKwh().multiply(verbruik));
                        totaalVerbruik = totaalVerbruik.add(verbruik);
                    }
                }
            } else {
                BigDecimal verbruik = meterstandRepository.getStroomVerbruikInPeriod(periodeVan, periodeTotEnMet);
                if (verbruik != null) {
                    totaalVerbruik = verbruik;
                }
            }
        }

        Verbruik verbruik = new Verbruik();
        verbruik.setVerbruik(totaalVerbruik);
        if (totaalVerbruik == null) {
            verbruik.setEuro(null);
        } else {
            verbruik.setEuro(totaalKosten.setScale(2, RoundingMode.CEILING));
        }
        return verbruik;
    }
}
