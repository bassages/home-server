package nl.wiegman.homecontrol.services.service.api;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.model.api.MeterstandOpDag;
import nl.wiegman.homecontrol.services.service.MeterstandService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Produces(MediaType.APPLICATION_JSON)
@Path(MeterstandServiceRest.SERVICE_PATH)
public class MeterstandServiceRest {

    public static final String SERVICE_PATH = "meterstanden";

    @Inject
    private MeterstandService meterstandService;

    @POST
    public void opslaanMeterstand(Meterstand meterstand) {
        meterstandService.opslaanMeterstand(meterstand);
    }

    @GET
    @Path("meestrecente")
    public Meterstand getMeestRecente() {
        return meterstandService.getMeestRecente();
    }

    @GET
    @Path("oudste")
    public Meterstand getOudste() {
        return meterstandService.getOudste();
    }

    @GET
    @Path("per-dag/{vanaf}/{totEnMet}")
    public List<MeterstandOpDag> perDag(@PathParam("vanaf") long vanaf, @PathParam("totEnMet") long totEnMet) {
        return meterstandService.perDag(vanaf, totEnMet);
    }
}
