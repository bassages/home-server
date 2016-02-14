package nl.wiegman.homecontrol.services.service.api;

import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import nl.wiegman.homecontrol.services.model.api.VerbruikOpDag;
import nl.wiegman.homecontrol.services.model.api.VerbruikPerMaandInJaar;
import nl.wiegman.homecontrol.services.service.VerbruikService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path(StroomServiceRest.SERVICE_PATH)
public class StroomServiceRest {

    public static final String SERVICE_PATH = "stroom";

    @Inject
    VerbruikService verbruikService;

    @GET
    @Path("verbruikPerMaandInJaar/{jaar}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<VerbruikPerMaandInJaar> getVerbruikPerMaandInJaar(@PathParam("jaar") int jaar) {
        return verbruikService.getVerbruikPerMaandInJaar(jaar);
    }

    @GET
    @Path("verbruikPerDag/{van}/{totEnMet}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<VerbruikOpDag> getVerbruikPerDag(@PathParam("van") long van, @PathParam("totEnMet") long totEnMet) {
        return verbruikService.getVerbruikPerDag(van, totEnMet);
    }

    @GET
    @Path("opgenomenVermogenHistorie/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OpgenomenVermogen> getOpgenomenVermogenHistory(@PathParam("from") long from, @PathParam("to") long to, @QueryParam("subPeriodLength") long subPeriodLength) {
        return verbruikService.getOpgenomenVermogenHistory(from, to, subPeriodLength);
    }
}