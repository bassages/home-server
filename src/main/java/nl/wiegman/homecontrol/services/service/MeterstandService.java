package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.model.event.UpdateEvent;
import nl.wiegman.homecontrol.services.repository.MeterstandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Path(MeterstandService.SERVICE_PATH)
public class MeterstandService {

    public static final String SERVICE_PATH = "meterstanden";

    private final Logger logger = LoggerFactory.getLogger(MeterstandService.class);

    @Inject
    private MeterstandRepository meterstandRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @POST
    @Path("opgenomenMeterstand")
    public void opslaanMeterstand(@FormParam("datumtijd") long datumtijd,
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

        logger.info("Save for " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(datumtijd)));
        meterstandRepository.save(meterstand);

        eventPublisher.publishEvent(new UpdateEvent(meterstand));
    }

    @GET
    @Path("meestrecente")
    @Produces(MediaType.APPLICATION_JSON)
    public Meterstand getMostRecent() {
        logger.info("getMostRecent()");
        return meterstandRepository.getMostRecentMeterstand();
    }

    @GET
    @Path("oudste")
    @Produces(MediaType.APPLICATION_JSON)
    public Meterstand getOldest() {
        logger.info("getOldest()");
        return meterstandRepository.getOldestMeterstand();
    }
}
