package nl.wiegman.home.service.api;

import nl.wiegman.home.model.VerbruikOpDag;
import nl.wiegman.home.model.VerbruikPerMaandInJaar;
import nl.wiegman.home.model.VerbruikPerUurOpDag;
import nl.wiegman.home.service.Energiesoort;
import nl.wiegman.home.service.VerbruikService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path(GasServiceRest.SERVICE_PATH)
public class GasServiceRest {

    public static final String SERVICE_PATH = "gas";

    @Inject
    VerbruikService verbruikService;

    @GET
    @Path("verbruik-per-maand-in-jaar/{jaar}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<VerbruikPerMaandInJaar> getVerbruikPerMaandInJaar(@PathParam("jaar") int jaar) {
        return verbruikService.getVerbruikPerMaandInJaar(Energiesoort.GAS, jaar);
    }

    @GET
    @Path("verbruik-per-dag/{van}/{totEnMet}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<VerbruikOpDag> getVerbruikPerDag(@PathParam("van") long van, @PathParam("totEnMet") long totEnMet) {
        return verbruikService.getVerbruikPerDag(Energiesoort.GAS, van, totEnMet);
    }

    @GET
    @Path("verbruik-per-uur-op-dag/{dag}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<VerbruikPerUurOpDag> getVerbruikPerUurOpDag(@PathParam("dag") long dag) {
        return verbruikService.getVerbruikPerUurOpDag(Energiesoort.GAS, dag);
    }
}