package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import nl.wiegman.homecontrol.services.model.event.UpdateEvent;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Api(value=ElektriciteitService.SERVICE_PATH, description="Onvangt en verspreid informatie over het elektriciteitsverbruik")
@Path(ElektriciteitService.SERVICE_PATH)
public class ElektriciteitService {

    public static final String SERVICE_PATH = "elektriciteit";

    private final Logger logger = LoggerFactory.getLogger(ElektriciteitService.class);

    @Inject
    private ElectriciteitStore electriciteitStore;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @ApiOperation(value = "Ontvangt het opgenomen vermogen op een bepaald moment")
    @POST
    @Path("opgenomenVermogen")
    public void opslaanAfgenomenVermogen(@FormParam("vermogenInWatt") int vermogenInWatt,
                                         @FormParam("datumtijd") @ApiParam(value = "Meetmoment in ISO8601 formaat") long datumtijd) {

        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setOpgenomenVermogenInWatt(vermogenInWatt);

        electriciteitStore.add(opgenomenVermogen);

        eventPublisher.publishEvent(new UpdateEvent(opgenomenVermogen));
    };

    @ApiOperation(value = "Geeft de historie van opgenomen vermogens terug")
    @GET
    @Path("opgenomenVermogenHistorie/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OpgenomenVermogen> getOpgenomenVermogenPerKwartier(@PathParam("from") long from, @PathParam("to") long to, @QueryParam("subPeriodLength") long subPeriodLength) {
        logger.info("getOpgenomenVermogenPerKwartier() from=" +from + " to=" + to + " subPeriodLength=" + subPeriodLength);

        List<OpgenomenVermogen> result = new ArrayList<>();

        long start = DateUtils.truncate(new Date(), Calendar.DATE).getTime();
        long end = DateUtils.truncate(DateUtils.addDays(new Date(), 1), Calendar.DATE).getTime();

        List<OpgenomenVermogen> list = electriciteitStore.getAll()
                                            .stream()
                                            .filter(ov -> ov.getDatumtijd() >= start && ov.getDatumtijd() < end)
                                            .sorted((ov1, ov2) -> Long.compare(ov1.getDatumtijd(), ov2.getDatumtijd()))
                                            .collect(Collectors.toList());

        long nrOfSubPeriodsInPeriod = (to-from)/subPeriodLength;

        for (int i=0; i<=nrOfSubPeriodsInPeriod; i++) {
            long subStart = start + (i * subPeriodLength);
            long subEnd = subStart + subPeriodLength;

            OpgenomenVermogen maximumOpgenomenVermogenInPeriode = getMaximumOpgenomenVermogenInPeriode(list, subStart, subEnd);
            if (maximumOpgenomenVermogenInPeriode != null) {
                maximumOpgenomenVermogenInPeriode.setDatumtijd(subStart);
                result.add(maximumOpgenomenVermogenInPeriode);
            } else {
                OpgenomenVermogen onbekend = new OpgenomenVermogen();
                onbekend.setDatumtijd(subStart);
                onbekend.setOpgenomenVermogenInWatt(0);
                result.add(onbekend);
            }
        }
        return result;
    }

    private OpgenomenVermogen getMaximumOpgenomenVermogenInPeriode(List<OpgenomenVermogen> list, long start, long end) {
        return list.stream()
                .filter(ov -> ov.getDatumtijd() >= start && ov.getDatumtijd() < end)
                .max((ov1, ov2) -> Integer.compare(ov1.getOpgenomenVermogenInWatt(), ov2.getOpgenomenVermogenInWatt()))
                .orElse(null);
    }

}