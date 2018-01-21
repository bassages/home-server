package nl.homeserver.dev.energie;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import nl.homeserver.energie.Dsmr42Reading;
import nl.homeserver.energie.Meterstand;
import nl.homeserver.energie.MeterstandService;
import nl.homeserver.energie.SlimmeMeterController;
import nl.homeserver.energie.StroomTariefIndicator;

@Service
public class SlimmeMeterSimulatorService extends AbstractDataGeneratorService {

    private final Logger logger = LoggerFactory.getLogger(SlimmeMeterSimulatorService.class);

    private final ScheduledExecutorService slimmeMeterSimulatorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> slimmeMeterSimulator = null;

    private BigDecimal lastGeneratedStroomTarief1 = null;
    private BigDecimal lastGeneratedStroomTarief2 = null;
    private BigDecimal lastGeneratedGas = null;

    @Value("${slimmeMeterSimulator.autostart}")
    private boolean autoStart;

    @Value("${slimmeMeterSimulator.initialDelaySeconds}")
    private int initialDelaySeconds;

    private final MeterstandService meterstandService;
    private final SlimmeMeterController slimmeMeterController;

    public SlimmeMeterSimulatorService(MeterstandService meterstandService, SlimmeMeterController slimmeMeterController) {
        this.meterstandService = meterstandService;
        this.slimmeMeterController = slimmeMeterController;
    }

    @PostConstruct
    public void init() {
        if (autoStart) {
            Meterstand mostRecent = meterstandService.getMostRecent();
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

    public void startSlimmeMeterSimulator() {
        if (slimmeMeterSimulator == null) {
            slimmeMeterSimulator = slimmeMeterSimulatorScheduler.scheduleAtFixedRate(this::simulateUpdateFromSlimmeMeter, initialDelaySeconds, SLIMME_METER_UPDATE_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    public void stopSlimmeMeterSimulator() {
        if (slimmeMeterSimulator != null) {
            slimmeMeterSimulator.cancel(false);
            slimmeMeterSimulator = null;
        }
    }

    private void simulateUpdateFromSlimmeMeter() {
        try {
            long datumtijd = System.currentTimeMillis();

            Dsmr42Reading dsmr42Reading = new Dsmr42Reading();

            dsmr42Reading.setDatumtijd(datumtijd);
            dsmr42Reading.setStroomTarief1(getStroomTarief1(datumtijd));
            dsmr42Reading.setStroomTarief2(getStroomTarief2(datumtijd));
            dsmr42Reading.setGas(getGas(datumtijd));
            dsmr42Reading.setStroomTariefIndicator((int) StroomTariefIndicator.NORMAAL.getId());
            dsmr42Reading.setStroomOpgenomenVermogenInWatt(getDummyVermogenInWatt());
            slimmeMeterController.save(dsmr42Reading);

        } catch (Exception e) {
            logger.error("Caught exception in ScheduledExecutorService.", e);
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
