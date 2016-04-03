package nl.wiegman.home.service.api;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.service.KlimaatService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Klimaat getMeestRecente() {
        return klimaatService.getMeestRecente();
    }

}
