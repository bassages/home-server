package nl.wiegman.home.service.api;

import nl.wiegman.home.model.Meterstand;
import nl.wiegman.home.model.MeterstandOpDag;
import nl.wiegman.home.service.MeterstandService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Component
@Produces(MediaType.APPLICATION_JSON)
@Path("meterstanden")
public class MeterstandServiceRest {

    @Inject
    private MeterstandService meterstandService;

    @POST
    public Response save(Meterstand meterstand) {
        meterstandService.save(meterstand);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("meest-recente")
    public Meterstand getMeestRecente() {
        return meterstandService.getMeestRecente();
    }

    @GET
    @Path("oudste")
    public Meterstand getOudste() {
        return meterstandService.getOudste();
    }

    @GET
    @Path("oudste-vandaag")
    public Meterstand getOudsteVandaag() {
        return meterstandService.getOudsteMeterstandOpDag(new Date());
    }

    @GET
    @Path("per-dag/{vanaf}/{totEnMet}")
    public List<MeterstandOpDag> perDag(@PathParam("vanaf") long vanaf, @PathParam("totEnMet") long totEnMet) {
        return meterstandService.perDag(vanaf, totEnMet);
    }

    @GET
    @Path("bestaat-op-datumtijd/{datumtijd}")
    public boolean bestaatOpDatumTijd(@PathParam("datumtijd") long datumtijd) {
        return meterstandService.bestaatOpDatumTijd(datumtijd);
    }

}
