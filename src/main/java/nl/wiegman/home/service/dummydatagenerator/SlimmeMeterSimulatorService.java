package nl.wiegman.home.service.dummydatagenerator;

import nl.wiegman.home.model.Meterstand;
import nl.wiegman.home.service.MeterstandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@Path(SlimmeMeterSimulatorService.SERVICE_PATH)
public class SlimmeMeterSimulatorService extends AbstractDataGeneratorService {

    private final Logger logger = LoggerFactory.getLogger(SlimmeMeterSimulatorService.class);

    public static final String SERVICE_PATH = "slimmemetersimulator";

    private final ScheduledExecutorService slimmeMeterSimulatorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> slimmeMeterSimulator = null;

    private BigDecimal lastGeneratedStroomTarief1 = null;
    private BigDecimal lastGeneratedStroomTarief2 = null;
    private BigDecimal lastGeneratedGas = null;

    @Value("${slimmeMeterSimulator.autostart}")
    boolean autoStart = false;

    @Autowired
    private MeterstandService meterstandService;

    @PostConstruct
    public void init() {
        if (autoStart) {
            Meterstand mostRecent = meterstandService.getMeestRecente();
            if (mostRecent == null) {
                lastGeneratedStroomTarief1 = INITIAL_GENERATOR_VALUE_STROOM;
                lastGeneratedStroomTarief2 = INITIAL_GENERATOR_VALUE_STROOM;
                lastGeneratedGas = INITIAL_GENERATOR_VALUE_GAS;
            } else {
                lastGeneratedStroomTarief1 = mostRecent.getStroomTarief1();
                lastGeneratedStroomTarief2 = mostRecent.getStroomTarief2();
                lastGeneratedGas = mostRecent.getGas();
            }
            startSlimmeMeterSimulator();
        }
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

            Meterstand meterstand = new Meterstand();
            meterstand.setDatumtijd(datumtijd);
            meterstand.setStroomTarief1(getStroomTarief1(datumtijd));
            meterstand.setStroomTarief2(getStroomTarief2(datumtijd));
            meterstand.setGas(getGas(datumtijd));
            meterstand.setStroomOpgenomenVermogenInWatt(getDummyVermogenInWatt());

            meterstandService.save(meterstand);
        } catch (Throwable t) {  // Catch Throwable rather than Exception (a subclass).
            logger.error("Caught exception in ScheduledExecutorService.", t);
        }
    }

    private BigDecimal getGas(long datumtijd) {
        lastGeneratedGas = lastGeneratedGas.add(getGasIncreasePerInterval(datumtijd));
        return lastGeneratedGas;
    }

    private BigDecimal getStroomTarief2(long datumtijd) {
        lastGeneratedStroomTarief2 = lastGeneratedStroomTarief2.add(getStroomIncreasePerInterval(datumtijd));
        return lastGeneratedStroomTarief2;
    }

    private BigDecimal getStroomTarief1(long datumtijd) {
        lastGeneratedStroomTarief1 = lastGeneratedStroomTarief2.add(getStroomIncreasePerInterval(datumtijd));
        return lastGeneratedStroomTarief1;
    }
}
