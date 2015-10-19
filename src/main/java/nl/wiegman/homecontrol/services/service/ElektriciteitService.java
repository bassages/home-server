package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nl.wiegman.homecontrol.services.apimodel.OpgenomenVermogen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Api(value=ElektriciteitService.SERVICE_PATH, description="Onvangt en verspreid informatie over het elektriciteitsverbruik")
@Path(ElektriciteitService.SERVICE_PATH)
public class ElektriciteitService {

    public static final String SERVICE_PATH = "elektriciteit";

    private final Logger logger = LoggerFactory.getLogger(ElektriciteitService.class);

    @Inject
    private ElectriciteitStore electriciteitStore;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @ApiOperation(value = "Ontvangt het opgenomen vermogen op een bepaald moment")
    @POST
    @Path("opgenomenVermogen")
    public void opslaanAfgenomenVermogen(@FormParam("vermogenInWatt") int vermogenInWatt,
                                         @FormParam("datumtijd") @ApiParam(value = "Meetmoment in ISO8601 formaat") long datumtijd) {

        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(datumtijd);
        opgenomenVermogen.setOpgenomenVermogenInWatt(vermogenInWatt);

        electriciteitStore.add(opgenomenVermogen);

        eventPublisher.publishEvent(opgenomenVermogen);
    };

    @ApiOperation(value = "Geeft de historie van opgenomen vermogens terug")
    @GET
    @Path("opgenomenVermogenHistorie")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OpgenomenVermogen> getOpgenomenVermogenHistorie() {
        return electriciteitStore.getAll();
    }
}