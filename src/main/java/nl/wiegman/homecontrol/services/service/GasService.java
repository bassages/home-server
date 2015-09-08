package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Component
@Api(value=GasService.SERVICE_PATH, description="Geeft informatie over elektra verbruik")
@Path(GasService.SERVICE_PATH)
public class GasService {
    public static final String SERVICE_PATH = "gas";

    @ApiOperation(value = "Geeft het huidige verbuik terug in liters per uur")
    @GET
    @Path("huidig")
    @Produces(MediaType.APPLICATION_JSON)
    public Integer huidigVerbruik() {
        return (int)(Math.random() * 2000) + 10;
    }

}
