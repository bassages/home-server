package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import nl.wiegman.homecontrol.services.model.api.StroomVerbruikOpDag;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Api(value=ElektriciteitService.SERVICE_PATH, description="Onvangt en verspreid informatie over het elektriciteitsverbruik")
@Path(ElektriciteitService.SERVICE_PATH)
public class ElektriciteitService {

    public static final String SERVICE_PATH = "elektriciteit";

    private final Logger logger = LoggerFactory.getLogger(ElektriciteitService.class);

    @Inject
    private MeterstandenStore meterstandenStore;

    @ApiOperation(value = "Geeft stroomverbruik per dag terug in de opgegeven periode")
    @GET
    @Path("verbruikPerDag/{van}/{totEnMet}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StroomVerbruikOpDag> getOpgenomenVermogenHistory(@PathParam("van") long van, @PathParam("totEnMet") long totEnMet) {
        List<StroomVerbruikOpDag> result = new ArrayList<>();

        List<Date> dagenInPeriode = getDagenInPeriode(van, totEnMet);
        for (Date dag : dagenInPeriode) {
            result.add(getStroomVerbruikOpDag(dag));
        }
        return result;
    }

    @ApiOperation(value = "Geeft de historie van opgenomen vermogens terug")
    @GET
    @Path("opgenomenVermogenHistorie/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(@PathParam("from") long from, @PathParam("to") long to, @QueryParam("subPeriodLength") long subPeriodLength) {
        logger.info("getOpgenomenVermogenHistory() from=" +from + " to=" + to + " subPeriodLength=" + subPeriodLength);

        List<OpgenomenVermogen> result = new ArrayList<>();

        List<Meterstand> list = meterstandenStore.getAll()
                .stream()
                .filter(ov -> ov.getDatumtijd() >= from && ov.getDatumtijd() < to)
                .sorted((ov1, ov2) -> Long.compare(ov1.getDatumtijd(), ov2.getDatumtijd()))
                .collect(Collectors.toList());

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

    private StroomVerbruikOpDag getStroomVerbruikOpDag(Date dag) {
        long van = dag.getTime();
        long totEnMet = DateUtils.addDays(dag, 1).getTime() - 1;

        List<Meterstand> meterstandenOpDag = Collections.unmodifiableList(meterstandenStore.getAll())
                .stream()
                .filter(ov -> ov.getDatumtijd() >= van && ov.getDatumtijd() <= totEnMet)
                .collect(Collectors.toList());

        Meterstand meterstandOpStartVanDag = meterstandenOpDag
                .stream()
                .max((ov1, ov2) -> Long.compare(ov2.getDatumtijd(), ov1.getDatumtijd()))
                .orElse(null);

        Meterstand meterstandOpEindVanDag = meterstandenOpDag
                .stream()
                .max((ov1, ov2) -> Long.compare(ov1.getDatumtijd(), ov2.getDatumtijd()))
                .orElse(null);

        int verbruikInKwh = 0;
        if (meterstandOpStartVanDag != null && meterstandOpEindVanDag != null) {
            verbruikInKwh = (meterstandOpEindVanDag.getStroomTarief1() - meterstandOpStartVanDag.getStroomTarief1())
                    + (meterstandOpEindVanDag.getStroomTarief2() - meterstandOpStartVanDag.getStroomTarief2());
        }
        StroomVerbruikOpDag stroomVerbruikOpDag = new StroomVerbruikOpDag();
        stroomVerbruikOpDag.setDt(dag.getTime());
        stroomVerbruikOpDag.setkWh(verbruikInKwh);
        stroomVerbruikOpDag.setEuro(verbruikInKwh * 0.2098);

        return stroomVerbruikOpDag;
    }

    public List<Date> getDagenInPeriode(long van, long totEnMet) {
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
        return  dagenInPeriode;
    }

    private OpgenomenVermogen getMaximumOpgenomenVermogenInPeriode(List<Meterstand> list, long start, long end) {
        return list.stream()
                .filter(ov -> ov.getDatumtijd() >= start && ov.getDatumtijd() < end)
                .map(m -> new OpgenomenVermogen(m.getDatumtijd(), m.getStroomOpgenomenVermogenInWatt()))
                .max((ov1, ov2) -> Integer.compare(ov1.getOpgenomenVermogenInWatt(), ov2.getOpgenomenVermogenInWatt()))
                .orElse(null);
    }
}