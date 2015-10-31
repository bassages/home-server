package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.model.api.OpgenomenVermogen;
import nl.wiegman.homecontrol.services.model.event.UpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Component
@Api(value=MeterstandService.SERVICE_PATH, description="Onvangt en verspreid informatie over meterstanden")
@Path(MeterstandService.SERVICE_PATH)
public class MeterstandService {

    public static final String SERVICE_PATH = "meterstanden";

    private final Logger logger = LoggerFactory.getLogger(MeterstandService.class);

    @Inject
    private MeterstandenStore meterstandenStore;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @ApiOperation(value = "Ontvangt een meterstand op een bepaald moment")
    @POST
    @Path("opgenomenMeterstand")
    public void opslaanMeterstand(@FormParam("datumtijd") @ApiParam(value = "Meetmoment in ISO8601 formaat") long datumtijd,
                                  @FormParam("stroomOpgenomenVermogenInWatt") int stroomOpgenomenVermogenInWatt,
                                  @FormParam("stroomTarief1") int stroomTarief1,
                                  @FormParam("stroomTarief2") int stroomTarief2,
                                  @FormParam("gas") int gas) {

        Meterstand meterstand = new Meterstand();
        meterstand.setDatumtijd(datumtijd);
        meterstand.setStroomOpgenomenVermogenInWatt(stroomOpgenomenVermogenInWatt);
        meterstand.setGas(gas);
        meterstand.setStroomTarief1(stroomTarief1);
        meterstand.setStroomTarief2(stroomTarief2);

        meterstandenStore.add(meterstand);

        eventPublisher.publishEvent(new UpdateEvent(meterstand));
    };

    @ApiOperation(value = "Geeft het meest recente opgenomen vermogen terug")
    @GET
    @Path("laatste")
    @Produces(MediaType.APPLICATION_JSON)
    public Meterstand getLaatste() {
        logger.info("getLaatste()");

        return meterstandenStore.getAll().stream()
                .max((m1, m2) -> Long.compare(m1.getDatumtijd(), m2.getDatumtijd()))
                .orElse(null);
    }

}
