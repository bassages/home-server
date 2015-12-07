package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import nl.wiegman.homecontrol.services.model.api.Meterstand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.concurrent.*;

@Component
@Api(value= SlimmeMeterSimulatorService.SERVICE_PATH, description="Genereert dummy data voor elektriciteitsverbruik")
@Path(SlimmeMeterSimulatorService.SERVICE_PATH)
public class SlimmeMeterSimulatorService {

    private final Logger logger = LoggerFactory.getLogger(SlimmeMeterSimulatorService.class);

    public static final String SERVICE_PATH = "slimmemetersimulator";

    public static final int SLIMME_METER_SIMULATOR_INTERVAL_IN_SECONDS = 10;

    public static final double STROOM_VERBRUIK_PER_INTERVAL = 0.001d;

    private final ScheduledExecutorService slimmeMeterSimulatorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> slimmeMeterSimulator = null;

    private int lastGeneratedOpgenomenVermogen = 50;

    private Double lastGeneratedStroomTarief1 = null;
    private Double lastGeneratedStroomTarief2 = null;

    @Autowired
    private MeterstandService meterstandService;

    @PostConstruct
    public void autoStart() {
        Meterstand mostRecent = meterstandService.getMostRecent();
        if (mostRecent == null) {
            lastGeneratedStroomTarief1 = 100000d;
            lastGeneratedStroomTarief2 = 100000d;
        } else {
            lastGeneratedStroomTarief1 = (double)mostRecent.getStroomTarief1();
            lastGeneratedStroomTarief2 = (double)mostRecent.getStroomTarief2();
        }
        startSlimmeMeterSimulator();
    }

    @POST
    @Path("startSlimmeMeterSimulator")
    public void startSlimmeMeterSimulator() {
        if (slimmeMeterSimulator == null) {
            slimmeMeterSimulator = slimmeMeterSimulatorScheduler.scheduleAtFixedRate(this::simulateUpdateFromSlimmeMeter, 0, SLIMME_METER_SIMULATOR_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
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

    private int getDummyVermogenInWatt() {
        int min = ThreadLocalRandom.current().nextInt(50, lastGeneratedOpgenomenVermogen + 1);
        int max = ThreadLocalRandom.current().nextInt(lastGeneratedOpgenomenVermogen, 1200 + 1);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void simulateUpdateFromSlimmeMeter() {
        meterstandService.opslaanMeterstand(System.currentTimeMillis(), getDummyVermogenInWatt(), getStroomTarief1(), getStroomTarief2(), 0);
    }

    private int getStroomTarief2() {
        lastGeneratedStroomTarief2 += STROOM_VERBRUIK_PER_INTERVAL;
        return (int)lastGeneratedStroomTarief2.doubleValue();
    }

    private int getStroomTarief1() {
        lastGeneratedStroomTarief1 += STROOM_VERBRUIK_PER_INTERVAL;
        return (int)lastGeneratedStroomTarief1.doubleValue();
    }
}
