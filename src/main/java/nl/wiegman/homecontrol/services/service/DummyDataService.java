package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@Api(value=DummyDataService.SERVICE_PATH, description="Onvangt en verspreid informatie over het elektriciteitsverbruik")
@Path(DummyDataService.SERVICE_PATH)
public class DummyDataService {

    public static final String SERVICE_PATH = "dummydata";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> scheduledFuture = null;

    private boolean oplopend = true;
    private int opgenomenVermogen = 0;

    @Autowired
    private ElektriciteitService elektriciteitService;

    @POST
    @Path("start")
    public void start() {
        if (scheduledFuture == null) {
            scheduledFuture = scheduler.scheduleAtFixedRate(this::sendDummyData, 0, 10, TimeUnit.SECONDS);
        }
    }

    @POST
    @Path("stop")
    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }

    private void sendDummyData() {
        elektriciteitService.opslaanAfgenomenVermogen(opgenomenVermogen, System.currentTimeMillis());

        if (oplopend) {
            opgenomenVermogen += 20;
            oplopend = opgenomenVermogen < 1400;
        } else {
            opgenomenVermogen -= 20;
            oplopend = opgenomenVermogen <= 0;
        }
    }
}
