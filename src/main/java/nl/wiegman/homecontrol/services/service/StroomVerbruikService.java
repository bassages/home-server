package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.model.api.Stroomverbruik;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class StroomVerbruikService {

    @Inject
    MeterstandRepository meterstandRepository;

    @Inject
    KostenRepository kostenRepository;

    public Stroomverbruik getVerbruikInPeriode(long periodeVan, long periodeTotEnMet) {
        BigDecimal totaalKosten = BigDecimal.ZERO;
        int totaalVerbruikInKwh = 0;

        if (periodeVan < System.currentTimeMillis()) {
            List<Kosten> kostenInPeriod = kostenRepository.getKostenInPeriod(periodeVan, periodeTotEnMet + 1);
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

                    Integer verbruik = meterstandRepository.getVerbruikInPeriod(subVanMillis, subTotEnMetMillis);
                    if (verbruik != null) {
                        totaalKosten = totaalKosten.add(kosten.getStroomPerKwh().multiply(new BigDecimal(verbruik)));
                        totaalVerbruikInKwh += verbruik;
                    }
                }
            } else {
                Integer verbruik = meterstandRepository.getVerbruikInPeriod(periodeVan, periodeTotEnMet);
                if (verbruik != null) {
                    totaalVerbruikInKwh = verbruik.intValue();
                }
            }
        }

        Stroomverbruik stroomverbruik = new Stroomverbruik();
        stroomverbruik.setkWh(totaalVerbruikInKwh);
        stroomverbruik.setEuro(totaalKosten.setScale(2, RoundingMode.CEILING));
        return stroomverbruik;
    }

    @Cacheable(cacheNames = "verbruikInPeriode")
    public Stroomverbruik getPotentiallyCachedVerbruikInPeriode(long vanMillis, long totEnMetMillis) {
        return getVerbruikInPeriode(vanMillis, totEnMetMillis);
    }
}
