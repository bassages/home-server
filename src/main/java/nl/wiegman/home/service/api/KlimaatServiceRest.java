package nl.wiegman.home.service.api;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.service.KlimaatService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Component
@Produces(MediaType.APPLICATION_JSON)
@Path("klimaat")
public class KlimaatServiceRest {

    @Inject
    private KlimaatService klimaatService;

    @POST
    public Response add(Klimaat klimaat) {
        klimaatService.add(klimaat);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("meest-recente")
    public Klimaat getMostRecent() {
        return klimaatService.getMostRecent();
    }

    @GET
    @Path("hoogste")
    public List<Klimaat> getHighest(@QueryParam("from") long from, @QueryParam("to") long to, @QueryParam("sensortype") String sensortype, @QueryParam("limit") int limit) {
        return klimaatService.getHighest(sensortype, new Date(from), new Date(to), limit);
    }

    @GET
    @Path("laagste")
    public List<Klimaat> getLowest(@QueryParam("from") long from, @QueryParam("to") long to, @QueryParam("sensortype") String sensortype, @QueryParam("limit") int limit) {
        return klimaatService.getLowest(sensortype, new Date(from), new Date(to), limit);
    }

    @GET
    @Path("get/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Klimaat> get(@PathParam("from") long from, @PathParam("to") long to) {
        return klimaatService.getInPeriod(new Date(from), new Date(to));
    }

}
