package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;

@Component
@Api(value=ElektriciteitService.SERVICE_PATH, description="Geeft informatie over gas verbruik")
@Path(ElektriciteitService.SERVICE_PATH)
public class ElektriciteitService {
    public static final String SERVICE_PATH = "elekticiteit";

    private final Logger logger = LoggerFactory.getLogger(ElektriciteitService.class);

    @ApiOperation(value = "Geeft het huidige verbuik terug in watt")
    @GET
    @Path("huidig")
    @Produces(MediaType.APPLICATION_JSON)
    public Integer huidigVerbruik() {
        return (int)(Math.random() * 2000) + 10;
    }

    @ApiOperation(value = "Geeft het huidige verbuik terug in watt")
    @POST
    @Path("afgenomenVermogen")
    public void afgenomenVermogen(@FormParam("vermogenInWatt") int vermogenInWatt) {
        logger.debug("afgenomenVermogen() watt: " + vermogenInWatt);
    };
}