package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.model.api.Stroomverbruik;
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
public class StroomVerbruikService {

    @Inject
    MeterstandRepository meterstandRepository;

    @Inject
    KostenRepository kostenRepository;

    @Cacheable(cacheNames = "stroomVerbruikInPeriode")
    public Stroomverbruik getPotentiallyCachedVerbruikInPeriode(long vanMillis, long totEnMetMillis) {
        return getVerbruikInPeriode(vanMillis, totEnMetMillis);
    }

    public Stroomverbruik getVerbruikInPeriode(long periodeVan, long periodeTotEnMet) {
        BigDecimal totaalKosten = BigDecimal.ZERO;
        Integer totaalVerbruikInKwh = null;

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

                    Integer verbruik = meterstandRepository.getVerbruikInPeriod(subVanMillis, subTotEnMetMillis);
                    if (verbruik != null) {
                        if (totaalVerbruikInKwh == null) {
                            totaalVerbruikInKwh = 0;
                        }
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
        if (totaalVerbruikInKwh == null) {
            stroomverbruik.setEuro(null);
        } else {
            stroomverbruik.setEuro(totaalKosten.setScale(2, RoundingMode.CEILING));
        }
        return stroomverbruik;
    }
}
