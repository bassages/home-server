package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import nl.wiegman.homecontrol.services.model.api.VerbruikOpDag;
import nl.wiegman.homecontrol.services.model.api.VerbruikPerMaandInJaar;
import nl.wiegman.homecontrol.services.model.api.Verbruik;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import nl.wiegman.homecontrol.services.repository.MeterstandRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.IntStream;

@Component
public class VerbruikService {

    private final Logger logger = LoggerFactory.getLogger(VerbruikService.class);

    @Inject
    MeterstandRepository meterstandRepository;

    @Inject
    KostenRepository kostenRepository;

    @Inject
    VerbruikServiceCached verbruikServiceCached;

    @Inject
    OpgenomenVermogenService opgenomenVermogenService;

    public List<VerbruikPerMaandInJaar> getVerbruikPerMaandInJaar(int jaar) {
        List<VerbruikPerMaandInJaar> result = new ArrayList<>();

        IntStream.rangeClosed(1, 12).forEach(
            maand -> result.add(getVerbruikInMaand(maand, jaar))
        );
        return result;
    }

    public List<VerbruikOpDag> getVerbruikPerDag(long van, long totEnMet) {
        List<VerbruikOpDag> result = new ArrayList<>();

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van, totEnMet);
        for (Date dag : dagenInPeriode) {
            logger.info("get verbruik op dag: " + dag);
            result.add(getStroomVerbruikOpDag(dag));
        }
        return result;
    }

    protected VerbruikPerMaandInJaar getVerbruikInMaand(int maand, int jaar) {
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

        Verbruik verbruikInPeriode = getVerbruikInPeriode(vanMillis, totEnMetMillis);

        VerbruikPerMaandInJaar verbruikPerMaandInJaar = new VerbruikPerMaandInJaar();
        verbruikPerMaandInJaar.setMaand(maand);
        verbruikPerMaandInJaar.setEuro(verbruikInPeriode.getEuro());

        if (verbruikInPeriode.getVerbruik() != null) {
            verbruikPerMaandInJaar.setVerbruik(verbruikInPeriode.getVerbruik().setScale(0, RoundingMode.HALF_UP));
        }

        return verbruikPerMaandInJaar;
    }

    private VerbruikOpDag getStroomVerbruikOpDag(Date dag) {
        long vanMillis = dag.getTime();
        long totEnMetMillis = DateUtils.addDays(dag, 1).getTime() - 1;

        Verbruik verbruikInPeriode = getVerbruikInPeriode(vanMillis, totEnMetMillis);

        VerbruikOpDag verbruikOpDag = new VerbruikOpDag();
        verbruikOpDag.setDt(dag.getTime());

        if (verbruikInPeriode.getVerbruik() != null) {
            verbruikOpDag.setVerbruik(verbruikInPeriode.getVerbruik());
        }

        verbruikOpDag.setEuro(verbruikInPeriode.getEuro());
        return verbruikOpDag;
    }

    private Verbruik getVerbruikInPeriode(long vanMillis, long totEnMetMillis) {
        if (totEnMetMillis < System.currentTimeMillis()) {
            return verbruikServiceCached.getPotentiallyCachedVerbruikInPeriode(vanMillis, totEnMetMillis);
        } else {
            return verbruikServiceCached.getVerbruikInPeriode(vanMillis, totEnMetMillis);
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