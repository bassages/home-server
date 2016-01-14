package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import nl.wiegman.homecontrol.services.repository.KostenRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("kosten")
public class KostenRest {

    @Inject
    KostenRepository kostenRepository;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Kosten> getAll() {
        return kostenRepository.findAll();
    }
}
