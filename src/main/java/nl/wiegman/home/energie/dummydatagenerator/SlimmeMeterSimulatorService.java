package nl.wiegman.home.energie.dummydatagenerator;

import nl.wiegman.home.energie.Meterstand;
import nl.wiegman.home.energie.StroomTariefIndicator;
import nl.wiegman.home.energie.MeterstandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RestController(SlimmeMeterSimulatorService.SERVICE_PATH)
public class SlimmeMeterSimulatorService extends AbstractDataGeneratorService {

    private final Logger logger = LoggerFactory.getLogger(SlimmeMeterSimulatorService.class);

    public static final String SERVICE_PATH = "slimmemetersimulator";

    private final ScheduledExecutorService slimmeMeterSimulatorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> slimmeMeterSimulator = null;

    private BigDecimal lastGeneratedStroomTarief1 = null;
    private BigDecimal lastGeneratedStroomTarief2 = null;
    private BigDecimal lastGeneratedGas = null;

    @Value("${slimmeMeterSimulator.autostart}")
    boolean autoStart;

    @Value("${slimmeMeterSimulator.initialDelaySeconds}")
    int initialDelaySeconds;

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
    
    @PostMapping(path = "startSlimmeMeterSimulator")
    public void startSlimmeMeterSimulator() {
        if (slimmeMeterSimulator == null) {
            slimmeMeterSimulator = slimmeMeterSimulatorScheduler.scheduleAtFixedRate(this::simulateUpdateFromSlimmeMeter, initialDelaySeconds, SLIMME_METER_UPDATE_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    @PostMapping(path = "stopSlimmeMeterSimulator")
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
            meterstand.setStroomTariefIndicator(StroomTariefIndicator.NORMAAL);
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
