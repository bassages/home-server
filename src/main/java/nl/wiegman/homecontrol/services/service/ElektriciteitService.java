package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Component
@Api(value=ElektriciteitService.SERVICE_PATH, description="Geeft informatie over gas verbruik")
@Path(ElektriciteitService.SERVICE_PATH)
public class ElektriciteitService {
    public static final String SERVICE_PATH = "elekticiteit";

    @ApiOperation(value = "Geeft het huidige verbuik terug in watt")
    @GET
    @Path("huidig")
    @Produces(MediaType.APPLICATION_JSON)
    public Integer huidigVerbruik() {
        return (int)(Math.random() * 2000) + 10;
    }
}