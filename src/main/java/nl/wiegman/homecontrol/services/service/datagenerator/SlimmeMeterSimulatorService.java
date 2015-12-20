package nl.wiegman.homecontrol.services.service.datagenerator;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.service.MeterstandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.concurrent.*;

@Component
@Path(SlimmeMeterSimulatorService.SERVICE_PATH)
public class SlimmeMeterSimulatorService extends AbstractDataGeneratorService {

    private final Logger logger = LoggerFactory.getLogger(SlimmeMeterSimulatorService.class);

    public static final String SERVICE_PATH = "slimmemetersimulator";

    private final ScheduledExecutorService slimmeMeterSimulatorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> slimmeMeterSimulator = null;

    private Double lastGeneratedStroomTarief1 = null;
    private Double lastGeneratedStroomTarief2 = null;

    @Autowired
    private MeterstandService meterstandService;

    @PostConstruct
    public void init() {
        Meterstand mostRecent = meterstandService.getMostRecent();
        if (mostRecent == null) {
            lastGeneratedStroomTarief1 = INITIAL_GENERATOR_VALUE_STROOM;
            lastGeneratedStroomTarief2 = INITIAL_GENERATOR_VALUE_STROOM;
        } else {
            lastGeneratedStroomTarief1 = (double)mostRecent.getStroomTarief1();
            lastGeneratedStroomTarief2 = (double)mostRecent.getStroomTarief2();
        }
//        startSlimmeMeterSimulator();
    }

    @POST
    @Path("startSlimmeMeterSimulator")
    public void startSlimmeMeterSimulator() {
        if (slimmeMeterSimulator == null) {
            long initialDelay = 30; // Give some time to the application to start up
            slimmeMeterSimulator = slimmeMeterSimulatorScheduler.scheduleAtFixedRate(this::simulateUpdateFromSlimmeMeter, initialDelay, SLIMME_METER_UPDATE_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    @POST
    @Path("stopSlimmeMeterSimulator")
    public void stopSlimmeMeterSimulator() {
        if (slimmeMeterSimulator != null) {
            slimmeMeterSimulator.cancel(false);
            slimmeMeterSimulator = null;
        }
    }

    private void simulateUpdateFromSlimmeMeter() {
        try {
            long datumtijd = System.currentTimeMillis();
            meterstandService.opslaanMeterstand(datumtijd, getDummyVermogenInWatt(), getStroomTarief1(datumtijd), getStroomTarief2(datumtijd), 0);
        } catch ( Throwable t ) {  // Catch Throwable rather than Exception (a subclass).
            logger.error("Caught exception in ScheduledExecutorService.", t);
        }
    }

    private int getStroomTarief2(long datumtijd) {
        lastGeneratedStroomTarief2 += getStroomInterval(datumtijd);
        return (int)lastGeneratedStroomTarief2.doubleValue();
    }

    private int getStroomTarief1(long datumtijd) {
        lastGeneratedStroomTarief1 += getStroomInterval(datumtijd);
        return (int)lastGeneratedStroomTarief1.doubleValue();
    }
}
