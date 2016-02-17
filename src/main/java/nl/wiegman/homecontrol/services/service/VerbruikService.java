package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.*;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import nl.wiegman.homecontrol.services.repository.MeterstandRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

    public List<VerbruikPerUurOpDag> getVerbruikPerUurOpDag(Energiesoort energiesoort, long dag) {
        List<VerbruikPerUurOpDag> result = new ArrayList<>();

        // TODO: Daylight saving....
        IntStream.rangeClosed(0, 23).forEach(
                uur -> result.add(getVerbruikInUur(energiesoort, new Date(dag), uur))
        );
        return result;
    }

    public List<VerbruikPerMaandInJaar> getVerbruikPerMaandInJaar(Energiesoort energiesoort, int jaar) {
        List<VerbruikPerMaandInJaar> result = new ArrayList<>();

        IntStream.rangeClosed(1, 12).forEach(
            maand -> result.add(getVerbruikInMaand(energiesoort, maand, jaar))
        );
        return result;
    }

    public List<VerbruikOpDag> getVerbruikPerDag(Energiesoort energiesoort, long van, long totEnMet) {
        List<VerbruikOpDag> result = new ArrayList<>();

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van, totEnMet);
        for (Date dag : dagenInPeriode) {
            logger.info("Get " + energiesoort.name() + " verbruik op dag: " + dag);

            result.add(getVerbruikOpDag(energiesoort, dag));
        }
        return result;
    }
    private VerbruikPerUurOpDag getVerbruikInUur(Energiesoort energiesoort, Date dag, int uur) {
        logger.info("Get " + energiesoort.name() + " verbruik in uur " + uur + " op dag: " + new SimpleDateFormat("dd-MM-yyyy").format(dag));

        VerbruikPerUurOpDag verbruikPerUurOpDag = new VerbruikPerUurOpDag();
        verbruikPerUurOpDag.setUur(uur);

        long vanMillis = DateTimeUtil.getStartOfDay(dag) + TimeUnit.HOURS.toMillis(uur);
        long totEnMetMillis = vanMillis + TimeUnit.HOURS.toMillis(1);
        Verbruik verbruikInPeriode = getVerbruikInPeriode(energiesoort, vanMillis, totEnMetMillis);

        if (verbruikInPeriode != null) {
            verbruikPerUurOpDag.setEuro(verbruikInPeriode.getEuro());
            verbruikPerUurOpDag.setVerbruik(verbruikInPeriode.getVerbruik());
        }
        return verbruikPerUurOpDag;
    }

    protected VerbruikPerMaandInJaar getVerbruikInMaand(Energiesoort energiesoort, int maand, int jaar) {
        logger.info("Get " + energiesoort.name() + " verbruik in maand: " + maand + "/" + jaar);

        Calendar van = Calendar.getInstance();
        van.set(Calendar.MONTH, maand - 1);
        van.set(Calendar.YEAR, jaar);
        van = DateUtils.truncate(van, Calendar.MONTH);
        final long vanMillis = van.getTimeInMillis();

        Calendar totEnMet = (Calendar) van.clone();
        totEnMet.add(Calendar.MONTH, 1);
        totEnMet.add(Calendar.MILLISECOND, -1);
        final long totEnMetMillis = totEnMet.getTimeInMillis();

        Verbruik verbruikInPeriode = getVerbruikInPeriode(energiesoort, vanMillis, totEnMetMillis);

        VerbruikPerMaandInJaar verbruikPerMaandInJaar = new VerbruikPerMaandInJaar();
        verbruikPerMaandInJaar.setMaand(maand);

        if (verbruikInPeriode.getVerbruik() != null) {
            verbruikPerMaandInJaar.setVerbruik(verbruikInPeriode.getVerbruik().setScale(0, RoundingMode.HALF_UP));
            verbruikPerMaandInJaar.setEuro(verbruikInPeriode.getEuro());
        }
        return verbruikPerMaandInJaar;
    }

    private VerbruikOpDag getVerbruikOpDag(Energiesoort energiesoort, Date dag) {
        long vanMillis = dag.getTime();
        long totEnMetMillis = DateUtils.addDays(dag, 1).getTime() - 1;

        Verbruik verbruikInPeriode = getVerbruikInPeriode(energiesoort, vanMillis, totEnMetMillis);

        VerbruikOpDag verbruikOpDag = new VerbruikOpDag();
        verbruikOpDag.setDt(dag.getTime());

        if (verbruikInPeriode.getVerbruik() != null) {
            verbruikOpDag.setVerbruik(verbruikInPeriode.getVerbruik());
        }

        verbruikOpDag.setEuro(verbruikInPeriode.getEuro());
        return verbruikOpDag;
    }

    private Verbruik getVerbruikInPeriode(Energiesoort energiesoort, long vanMillis, long totEnMetMillis) {
        if (totEnMetMillis < System.currentTimeMillis()) {
            return verbruikServiceCached.getPotentiallyCachedVerbruikInPeriode(energiesoort, vanMillis, totEnMetMillis);
        } else {
            return verbruikServiceCached.getVerbruikInPeriode(energiesoort, vanMillis, totEnMetMillis);
        }
    }

    public List<OpgenomenVermogen> getOpgenomenStroomVermogenHistory(long from, long to, long subPeriodLength) {
        if (to < System.currentTimeMillis()) {
            return opgenomenVermogenService.getPotentiallyCachedOpgenomenStroomVermogenHistory(from, to, subPeriodLength);
        } else {
            return opgenomenVermogenService.getOpgenomenStroomVermogenHistory(from, to, subPeriodLength);
        }
    }
}