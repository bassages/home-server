package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.*;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.IntStream;

@Component
@Path(ElektriciteitService.SERVICE_PATH)
public class ElektriciteitService {

    public static final String SERVICE_PATH = "elektriciteit";
    public static final double STROOMKOSTEN_PER_KWH = 0.2098;

    private final Logger logger = LoggerFactory.getLogger(ElektriciteitService.class);

    @Inject
    private MeterstandRepository meterstandRepository;

    @GET
    @Path("verbruikPerMaandInJaar/{jaar}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StroomVerbruikPerMaandInJaar> getVerbruikPerMaandInJaar(@PathParam("jaar") int jaar) {
        List<StroomVerbruikPerMaandInJaar> result = new ArrayList<>();

        IntStream.rangeClosed(1, 12).forEach(
            maand -> result.add(getStroomVerbruikInMaand(maand, jaar))
        );
        return result;
    }

    @GET
    @Path("verbruikPerWeekInJaar/{jaar}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StroomVerbruikPerWeekInJaar> getVerbruikPerWeekInJaar(@PathParam("jaar") int jaar) {
        List<StroomVerbruikPerWeekInJaar> result = new ArrayList<>();

        int totalWeeksInYear = getTotalWeeksInYear(jaar);
        IntStream.rangeClosed(1, 53).forEach(
                maand -> result.add(getStroomVerbruikInWeek(maand, jaar))
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

    private StroomVerbruikPerMaandInJaar getStroomVerbruikInMaand(int maand, int jaar) {
        logger.info("get verbruik in maand: " + maand + "/" + jaar);

        Calendar start = Calendar.getInstance();
        start.set(Calendar.MONTH, maand-1);
        start.set(Calendar.YEAR, jaar);
        start = DateUtils.truncate(start, Calendar.MONTH);
        final long startMillis = start.getTimeInMillis();

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.MILLISECOND, -1);
        final long endMillis = end.getTimeInMillis();

        final Integer verbruik = meterstandRepository.getVerbruikInPeriod(startMillis, endMillis);

        StroomVerbruikPerMaandInJaar stroomVerbruikPerMaandInJaar = new StroomVerbruikPerMaandInJaar();
        stroomVerbruikPerMaandInJaar.setMaand(maand);

        if (verbruik != null) {
            stroomVerbruikPerMaandInJaar.setEuro(verbruik * STROOMKOSTEN_PER_KWH);
            stroomVerbruikPerMaandInJaar.setkWh(verbruik);
        }

        return stroomVerbruikPerMaandInJaar;
    }

    private StroomVerbruikPerWeekInJaar getStroomVerbruikInWeek(int week, int jaar) {
        logger.info("get verbruik in week: " + week + "/" + jaar);

//        Calendar start = Calendar.getInstance();
//        start.set(Calendar.MONTH, maand-1);
//        start.set(Calendar.YEAR, jaar);
//        start = DateUtils.truncate(start, Calendar.MONTH);
//        final long startMillis = start.getTimeInMillis();
//
//        Calendar end = (Calendar) start.clone();
//        end.add(Calendar.MONTH, 1);
//        end.add(Calendar.MILLISECOND, -1);
//        final long endMillis = end.getTimeInMillis();
//
//        final Integer verbruik = meterstandRepository.getVerbruikInPeriod(startMillis, endMillis);

        StroomVerbruikPerWeekInJaar stroomVerbruikPerMaandInJaar = new StroomVerbruikPerWeekInJaar();
        stroomVerbruikPerMaandInJaar.setWeek(week);

//        if (verbruik != null) {
            stroomVerbruikPerMaandInJaar.setEuro(week * STROOMKOSTEN_PER_KWH);
            stroomVerbruikPerMaandInJaar.setkWh(week);
//        }

        return stroomVerbruikPerMaandInJaar;
    }

    private StroomVerbruikOpDag getStroomVerbruikOpDag(Date dag) {
        long startMillis = dag.getTime();
        long endMillis = DateUtils.addDays(dag, 1).getTime() - 1;

        final Integer verbruik = meterstandRepository.getVerbruikInPeriod(startMillis, endMillis);

        StroomVerbruikOpDag stroomVerbruikOpDag = new StroomVerbruikOpDag();
        stroomVerbruikOpDag.setDt(dag.getTime());
        if (verbruik != null) {
            stroomVerbruikOpDag.setEuro(verbruik * STROOMKOSTEN_PER_KWH);
            stroomVerbruikOpDag.setkWh(verbruik);
        }

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

    private OpgenomenVermogen getMaximumOpgenomenVermogenInPeriode(List<Meterstand> list, long start, long end) {
        return list.stream()
                .filter(ov -> ov.getDatumtijd() >= start && ov.getDatumtijd() < end)
                .map(m -> new OpgenomenVermogen(m.getDatumtijd(), m.getStroomOpgenomenVermogenInWatt()))
                .max((ov1, ov2) -> Integer.compare(ov1.getOpgenomenVermogenInWatt(), ov2.getOpgenomenVermogenInWatt()))
                .orElse(null);
    }

    private int getTotalWeeksInYear(int year) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        mCalendar.set(Calendar.DAY_OF_MONTH, 31);
        return mCalendar.get(Calendar.WEEK_OF_YEAR);
    }
}