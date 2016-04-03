package nl.wiegman.home.service.api;

import nl.wiegman.home.model.Kosten;
import nl.wiegman.home.service.KostenService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("kosten")
@Produces(MediaType.APPLICATION_JSON)
public class KostenServiceRest {

    @Inject
    KostenService kostenService;

    @GET
    public List<Kosten> getAll() {
        return kostenService.getAll();
    }

    @POST
    public Kosten save(Kosten kosten) {
        return kostenService.save(kosten);
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") long id) {
        kostenService.delete(id);
    }
}
