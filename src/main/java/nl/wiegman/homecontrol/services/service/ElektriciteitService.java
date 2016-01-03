package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.IntStream;

@Component
@Path(ElektriciteitService.SERVICE_PATH)
public class ElektriciteitService {

    public static final String SERVICE_PATH = "elektriciteit";

    private final Logger logger = LoggerFactory.getLogger(ElektriciteitService.class);

    @Inject
    MeterstandRepository meterstandRepository;

    @Inject
    KostenRepository kostenRepository;

    @GET
    @Path("verbruikPerMaandInJaar/{jaar}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StroomVerbruikPerMaandInJaar> getVerbruikPerMaandInJaar(@PathParam("jaar") int jaar) {
        List<StroomVerbruikPerMaandInJaar> result = new ArrayList<>();

        IntStream.rangeClosed(1, 12).forEach(
            maand -> result.add(getStroomverbruikInMaand(maand, jaar))
        );
        return result;
    }

    @GET
    @Path("verbruikPerDag/{van}/{totEnMet}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StroomVerbruikOpDag> getVerbruikPerDag(@PathParam("van") long van, @PathParam("totEnMet") long totEnMet) {
        List<StroomVerbruikOpDag> result = new ArrayList<>();

        List<Date> dagenInPeriode = getDagenInPeriode(van, totEnMet);
        for (Date dag : dagenInPeriode) {
            logger.info("get verbruik op dag: " + dag);
            result.add(getStroomVerbruikOpDag(dag));
        }
        return result;
    }

    @GET
    @Path("opgenomenVermogenHistorie/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(@PathParam("from") long from, @PathParam("to") long to, @QueryParam("subPeriodLength") long subPeriodLength) {
        List<OpgenomenVermogen> result = new ArrayList<>();

        List<Meterstand> list = meterstandRepository.getMeterstanden(from, to);

        long nrOfSubPeriodsInPeriod = (to-from)/subPeriodLength;

        for (int i=0; i<=nrOfSubPeriodsInPeriod; i++) {
            long subStart = from + (i * subPeriodLength);
            long subEnd = subStart + subPeriodLength;

            OpgenomenVermogen vermogenInPeriode = getMaximumOpgenomenVermogenInPeriode(list, subStart, subEnd);
            if (vermogenInPeriode != null) {
                vermogenInPeriode.setDatumtijd(subStart);
                result.add(vermogenInPeriode);
            } else {
                result.add(new OpgenomenVermogen(subStart, 0));
            }
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

    protected Stroomverbruik getVerbruikInPeriode(long periodeVan, long periodeTotEnMet) {
        BigDecimal totaalKosten = BigDecimal.ZERO;
        int totaalVerbruikInKwh = 0;

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

        Stroomverbruik stroomverbruik = new Stroomverbruik();
        stroomverbruik.setkWh(totaalVerbruikInKwh);
        stroomverbruik.setEuro(totaalKosten.setScale(2, RoundingMode.CEILING));
        return stroomverbruik;
    }

    private OpgenomenVermogen getMaximumOpgenomenVermogenInPeriode(List<Meterstand> list, long start, long end) {
        return list.stream()
                .filter(ov -> ov.getDatumtijd() >= start && ov.getDatumtijd() < end)
                .map(m -> new OpgenomenVermogen(m.getDatumtijd(), m.getStroomOpgenomenVermogenInWatt()))
                .max((ov1, ov2) -> Integer.compare(ov1.getOpgenomenVermogenInWatt(), ov2.getOpgenomenVermogenInWatt()))
                .orElse(null);
    }
}