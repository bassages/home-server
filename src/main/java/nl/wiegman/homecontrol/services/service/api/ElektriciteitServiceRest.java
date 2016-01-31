package nl.wiegman.homecontrol.services.service.api;

import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import nl.wiegman.homecontrol.services.model.api.StroomVerbruikOpDag;
import nl.wiegman.homecontrol.services.model.api.StroomVerbruikPerMaandInJaar;
import nl.wiegman.homecontrol.services.service.ElektriciteitService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path(ElektriciteitServiceRest.SERVICE_PATH)
public class ElektriciteitServiceRest {

    public static final String SERVICE_PATH = "elektriciteit";

    @Inject
    ElektriciteitService elektriciteitService;

    @GET
    @Path("verbruikPerMaandInJaar/{jaar}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StroomVerbruikPerMaandInJaar> getVerbruikPerMaandInJaar(@PathParam("jaar") int jaar) {
        return elektriciteitService.getVerbruikPerMaandInJaar(jaar);
    }

    @GET
    @Path("verbruikPerDag/{van}/{totEnMet}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StroomVerbruikOpDag> getVerbruikPerDag(@PathParam("van") long van, @PathParam("totEnMet") long totEnMet) {
        return elektriciteitService.getVerbruikPerDag(van, totEnMet);
    }

    @GET
    @Path("opgenomenVermogenHistorie/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(@PathParam("from") long from, @PathParam("to") long to, @QueryParam("subPeriodLength") long subPeriodLength) {
        return elektriciteitService.getOpgenomenVermogenHistory(from, to, subPeriodLength);
    }
}