package nl.wiegman.homecontrol.services.service.api;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import nl.wiegman.homecontrol.services.service.CacheService;
import nl.wiegman.homecontrol.services.service.KostenService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
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
