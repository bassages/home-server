package nl.wiegman.home.service.dummydatagenerator;

import nl.wiegman.home.model.api.Meterstand;
import nl.wiegman.home.repository.MeterstandRepository;
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
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@Path(HistoricDataGeneratorService.SERVICE_PATH)
public class HistoricDataGeneratorService extends AbstractDataGeneratorService {

    private final Logger logger = LoggerFactory.getLogger(HistoricDataGeneratorService.class);

    public static final String SERVICE_PATH = "historicdatagenerator";

    public static final int GENERATOR_RUN_INTERVAL_IN_SECONDS = 1;

    private final ScheduledExecutorService historischeDataGeneratorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> historischeDataGenerator = null;

    private BigDecimal lastGeneratedStroomTarief1 = null;
    private BigDecimal lastGeneratedStroomTarief2 = null;
    private BigDecimal lastGeneratedGas = null;
    private Long lastGeneratedTimestamp = null;

    @Value("${historicDataGenerator.autostart}")
    boolean autoStart = false;

    @Autowired
    private MeterstandService meterstandService;

    @Autowired
    private MeterstandRepository meterstandRepository;

    @PostConstruct
    public void init() {
        if (autoStart) {
            Meterstand oldest = meterstandService.getOudste();
            if (oldest == null) {
                lastGeneratedStroomTarief1 = INITIAL_GENERATOR_VALUE_STROOM;
                lastGeneratedStroomTarief2 = INITIAL_GENERATOR_VALUE_STROOM;
                lastGeneratedGas = INITIAL_GENERATOR_VALUE_GAS;
                lastGeneratedTimestamp = System.currentTimeMillis();
            } else {
                lastGeneratedStroomTarief1 = oldest.getStroomTarief1();
                lastGeneratedStroomTarief2 = oldest.getStroomTarief2();
                lastGeneratedGas = oldest.getGas();
                lastGeneratedTimestamp = oldest.getDatumtijd();
            }
            startGeneratingHistoricData();
        }
    }

    @POST
    @Path("startGeneratingHistoricData")
    public void startGeneratingHistoricData() {
        if (historischeDataGenerator == null) {
            long initialDelay = 30; // Give some time to the application to start up
            historischeDataGenerator = historischeDataGeneratorScheduler.scheduleAtFixedRate(this::generateHistoricData, initialDelay, GENERATOR_RUN_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    @POST
    @Path("stopGeneratingHistoricData")
    public void stopGeneratingHistoricData() {
        if (historischeDataGenerator != null) {
            historischeDataGenerator.cancel(false);
            historischeDataGenerator = null;
        }
    }

    private void generateHistoricData() {
        try {
            lastGeneratedTimestamp -= TimeUnit.SECONDS.toMillis(SLIMME_METER_UPDATE_INTERVAL_IN_SECONDS);
            lastGeneratedStroomTarief1 = lastGeneratedStroomTarief1.subtract(getStroomIncreasePerInterval(lastGeneratedTimestamp));
            lastGeneratedStroomTarief2 = lastGeneratedStroomTarief2.subtract(getStroomIncreasePerInterval(lastGeneratedTimestamp));
            lastGeneratedGas = lastGeneratedGas.subtract(getGasIncreasePerInterval(lastGeneratedTimestamp));

            Meterstand meterstand = new Meterstand();
            meterstand.setDatumtijd(lastGeneratedTimestamp);
            meterstand.setStroomOpgenomenVermogenInWatt(getDummyVermogenInWatt());
            meterstand.setGas(lastGeneratedGas.setScale(3, RoundingMode.CEILING));
            meterstand.setStroomTarief1(lastGeneratedStroomTarief2.setScale(3, RoundingMode.CEILING));
            meterstand.setStroomTarief2(lastGeneratedStroomTarief1.setScale(3, RoundingMode.CEILING));

            logger.info("Add historic data for " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(lastGeneratedTimestamp)));
            meterstandRepository.save(meterstand);

        } catch ( Throwable t ) {  // Catch Throwable rather than Exception (a subclass).
            logger.error("Caught exception in ScheduledExecutorService.", t);
        }
    }
}
