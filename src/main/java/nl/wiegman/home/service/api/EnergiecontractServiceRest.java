package nl.wiegman.home.service.api;

import nl.wiegman.home.model.Energiecontract;
import nl.wiegman.home.service.EnergiecontractService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("energiecontract")
@Produces(MediaType.APPLICATION_JSON)
public class EnergiecontractServiceRest {

    @Inject
    EnergiecontractService energiecontractService;

    @GET
    public List<Energiecontract> getAll() {
        return energiecontractService.getAll();
    }

    @POST
    public Energiecontract save(Energiecontract energiecontract) {
        return energiecontractService.save(energiecontract);
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") long id) {
        energiecontractService.delete(id);
    }
}
