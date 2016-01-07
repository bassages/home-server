package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import nl.wiegman.homecontrol.services.model.api.StroomVerbruikOpDag;
import nl.wiegman.homecontrol.services.model.api.StroomVerbruikPerMaandInJaar;
import nl.wiegman.homecontrol.services.model.api.Stroomverbruik;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.IntStream;

@Component
public class ElektriciteitService {

    private final Logger logger = LoggerFactory.getLogger(ElektriciteitService.class);

    @Inject
    MeterstandRepository meterstandRepository;

    @Inject
    KostenRepository kostenRepository;

    @Inject
    StroomVerbruikService stroomVerbruikService;

    @Inject
    OpgenomenVermogenService opgenomenVermogenService;

    public List<StroomVerbruikPerMaandInJaar> getVerbruikPerMaandInJaar(int jaar) {
        List<StroomVerbruikPerMaandInJaar> result = new ArrayList<>();

        IntStream.rangeClosed(1, 12).forEach(
            maand -> result.add(getStroomverbruikInMaand(maand, jaar))
        );
        return result;
    }

    public List<StroomVerbruikOpDag> getVerbruikPerDag(long van, long totEnMet) {
        List<StroomVerbruikOpDag> result = new ArrayList<>();

        List<Date> dagenInPeriode = getDagenInPeriode(van, totEnMet);
        for (Date dag : dagenInPeriode) {
            logger.info("get verbruik op dag: " + dag);
            result.add(getStroomVerbruikOpDag(dag));
        }
        return result;
    }

    protected StroomVerbruikPerMaandInJaar getStroomverbruikInMaand(int maand, int jaar) {
        logger.info("Get verbruik in maand: " + maand + "/" + jaar);

        Calendar van = Calendar.getInstance();
        van.set(Calendar.MONTH, maand - 1);
        van.set(Calendar.YEAR, jaar);
        van = DateUtils.truncate(van, Calendar.MONTH);
        final long vanMillis = van.getTimeInMillis();

        Calendar totEnMet = (Calendar) van.clone();
        totEnMet.add(Calendar.MONTH, 1);
        totEnMet.add(Calendar.MILLISECOND, -1);
        final long totEnMetMillis = totEnMet.getTimeInMillis();

        Stroomverbruik verbruikInPeriode = getVerbruikInPeriode(vanMillis, totEnMetMillis);

        StroomVerbruikPerMaandInJaar stroomVerbruikPerMaandInJaar = new StroomVerbruikPerMaandInJaar();
        stroomVerbruikPerMaandInJaar.setMaand(maand);
        stroomVerbruikPerMaandInJaar.setEuro(verbruikInPeriode.getEuro());
        stroomVerbruikPerMaandInJaar.setkWh(verbruikInPeriode.getkWh());
        return stroomVerbruikPerMaandInJaar;
    }

    private StroomVerbruikOpDag getStroomVerbruikOpDag(Date dag) {
        long vanMillis = dag.getTime();
        long totEnMetMillis = DateUtils.addDays(dag, 1).getTime() - 1;

        Stroomverbruik verbruikInPeriode = getVerbruikInPeriode(vanMillis, totEnMetMillis);

        StroomVerbruikOpDag stroomVerbruikOpDag = new StroomVerbruikOpDag();
        stroomVerbruikOpDag.setDt(dag.getTime());
        stroomVerbruikOpDag.setkWh(verbruikInPeriode.getkWh());
        stroomVerbruikOpDag.setEuro(verbruikInPeriode.getEuro());
        return stroomVerbruikOpDag;
    }

    protected List<Date> getDagenInPeriode(long van, long totEnMet) {
        List<Date> dagenInPeriode = new ArrayList<>();

        Date datumVan = DateUtils.truncate(new Date(van), Calendar.DATE);
        Date datumTotEnMet = DateUtils.truncate(new Date(totEnMet), Calendar.DATE);

        Date datum = datumVan;

        while (true) {
            dagenInPeriode.add(datum);

            if (DateUtils.isSameDay(datum, datumTotEnMet)) {
                break;
            } else {
                datum = DateUtils.addDays(datum, 1);
            }
        }
        Collections.reverse(dagenInPeriode);
        return dagenInPeriode;
    }

    private Stroomverbruik getVerbruikInPeriode(long vanMillis, long totEnMetMillis) {
        if (totEnMetMillis < System.currentTimeMillis()) {
            return stroomVerbruikService.getPotentiallyCachedVerbruikInPeriode(vanMillis, totEnMetMillis);
        } else {
            return stroomVerbruikService.getVerbruikInPeriode(vanMillis, totEnMetMillis);
        }
    }

    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(long from, long to, long subPeriodLength) {
        if (to < System.currentTimeMillis()) {
            return opgenomenVermogenService.getPotentiallyCachedOpgenomenVermogenHistory(from, to, subPeriodLength);
        } else {
            return opgenomenVermogenService.getOpgenomenVermogenHistory(from, to, subPeriodLength);
        }
    }
}