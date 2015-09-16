package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nl.wiegman.homecontrol.services.apimodel.OpgenomenVermogen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Api(value=ElektriciteitService.SERVICE_PATH, description="Geeft informatie over elektriciteitsverbruik")
@Path(ElektriciteitService.SERVICE_PATH)
public class ElektriciteitService {
    public static final String SERVICE_PATH = "elektriciteit";

    private final Logger logger = LoggerFactory.getLogger(ElektriciteitService.class);

    @Inject
    private ElectriciteitStore electriciteitStore;

    @ApiOperation(value = "Geeft het huidige verbuik terug in watt")
    @GET
    @Path("huidig")
    @Produces(MediaType.APPLICATION_JSON)
    public Integer huidigVerbruik() {
        return (int)(Math.random() * 2000) + 10;
    }

    @ApiOperation(value = "Sla het opgenomen vermogen op een bepaald moment op")
    @POST
    @Path("opgenomenVermogen")
    public void opslaanAfgenomenVermogen(@FormParam("vermogenInWatt") int vermogenInWatt,
                                         @FormParam("datumtijd") @ApiParam(value = "Meetmoment in ISO8601 formaat") long datumtijd) {
        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setOpgenomenVermogenInWatt(vermogenInWatt);

        electriciteitStore.add(opgenomenVermogen);
    };

    @ApiOperation(value = "Geeft de historie van opgenomen vermogens terug")
    @GET
    @Path("opgenomenVermogenHistorie")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OpgenomenVermogen> getOpgenomenVermogenHistorie() {
        return electriciteitStore.getAll();
    }
}