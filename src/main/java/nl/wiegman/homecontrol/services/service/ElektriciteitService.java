package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import nl.wiegman.homecontrol.services.model.api.StroomVerbruikOpDag;
import nl.wiegman.homecontrol.services.model.api.StroomVerbruikPerMaandInJaar;
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
@Api(value=ElektriciteitService.SERVICE_PATH, description="Onvangt en verspreid informatie over het elektriciteitsverbruik")
@Path(ElektriciteitService.SERVICE_PATH)
public class ElektriciteitService {

    public static final String SERVICE_PATH = "elektriciteit";
    public static final double STROOMKOSTEN_PER_KWH = 0.2098;

    private final Logger logger = LoggerFactory.getLogger(ElektriciteitService.class);

    @Inject
    private MeterstandenRepository meterstandenRepository;

    @ApiOperation(value = "Geeft stroomverbruik per maand terug in het opgegeven jaar")
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

    @ApiOperation(value = "Geeft stroomverbruik per dag terug in de opgegeven periode")
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

    @ApiOperation(value = "Geeft de historie van opgenomen vermogens terug")
    @GET
    @Path("opgenomenVermogenHistorie/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(@PathParam("from") long from, @PathParam("to") long to, @QueryParam("subPeriodLength") long subPeriodLength) {
        List<OpgenomenVermogen> result = new ArrayList<>();

        List<Meterstand> list = meterstandenRepository.getMeterstanden(from, to);

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

        final Integer verbruik = meterstandenRepository.getVerbruikInPeriod(startMillis, endMillis);

        StroomVerbruikPerMaandInJaar stroomVerbruikPerMaandInJaar = new StroomVerbruikPerMaandInJaar();
        stroomVerbruikPerMaandInJaar.setMaand(maand);

        if (verbruik != null) {
            stroomVerbruikPerMaandInJaar.setEuro(verbruik * STROOMKOSTEN_PER_KWH);
            stroomVerbruikPerMaandInJaar.setkWh(verbruik);
        }

        return stroomVerbruikPerMaandInJaar;
    }

    private StroomVerbruikOpDag getStroomVerbruikOpDag(Date dag) {
        long startMillis = dag.getTime();
        long endMillis = DateUtils.addDays(dag, 1).getTime() - 1;

        final Integer verbruik = meterstandenRepository.getVerbruikInPeriod(startMillis, endMillis);

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
}