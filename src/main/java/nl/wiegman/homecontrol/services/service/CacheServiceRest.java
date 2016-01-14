package nl.wiegman.homecontrol.services.service;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Component
@Path("cache")
public class CacheServiceRest {

    @Inject
    CacheService cacheService;

    @POST
    @Path("clearAll")
    @Produces(MediaType.APPLICATION_JSON)
    public void clearAll() {
        cacheService.clearAll();
    }
}