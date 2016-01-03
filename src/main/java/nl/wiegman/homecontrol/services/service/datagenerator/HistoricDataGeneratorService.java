package nl.wiegman.homecontrol.services.service.datagenerator;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import nl.wiegman.homecontrol.services.service.MeterstandRepository;
import nl.wiegman.homecontrol.services.service.MeterstandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

@Component
@Path(HistoricDataGeneratorService.SERVICE_PATH)
public class HistoricDataGeneratorService extends AbstractDataGeneratorService {

    private final Logger logger = LoggerFactory.getLogger(HistoricDataGeneratorService.class);

    public static final String SERVICE_PATH = "historicdatagenerator";

    public static final int GENERATOR_RUN_INTERVAL_IN_SECONDS = 1;

    private final ScheduledExecutorService historischeDataGeneratorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> historischeDataGenerator = null;

    private Double lastGeneratedStroomTarief1 = null;
    private Double lastGeneratedStroomTarief2 = null;
    private Long lastGeneratedTimestamp = null;

    @Autowired
    private MeterstandService meterstandService;

    @Autowired
    private MeterstandRepository meterstandRepository;

    @PostConstruct
    public void init() {
        Meterstand oldest = meterstandService.getOldest();
        if (oldest == null) {
            lastGeneratedStroomTarief1 = INITIAL_GENERATOR_VALUE_STROOM;
            lastGeneratedStroomTarief2 = INITIAL_GENERATOR_VALUE_STROOM;
            lastGeneratedTimestamp = System.currentTimeMillis();
        } else {
            lastGeneratedStroomTarief1 = (double)oldest.getStroomTarief1();
            lastGeneratedStroomTarief2 = (double)oldest.getStroomTarief2();
            lastGeneratedTimestamp = oldest.getDatumtijd();
        }
//        startGeneratingHistoricData();
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
            lastGeneratedStroomTarief1 += getStroomInterval(lastGeneratedTimestamp);
            lastGeneratedStroomTarief2 += getStroomInterval(lastGeneratedTimestamp);

            Meterstand meterstand = new Meterstand();
            meterstand.setDatumtijd(lastGeneratedTimestamp);
            meterstand.setStroomOpgenomenVermogenInWatt(getDummyVermogenInWatt());
            meterstand.setGas(0);
            meterstand.setStroomTarief1((int)lastGeneratedStroomTarief2.doubleValue());
            meterstand.setStroomTarief2((int)lastGeneratedStroomTarief1.doubleValue());

            logger.info("Add historic data for " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(lastGeneratedTimestamp)));
            meterstandRepository.save(meterstand);

        } catch ( Throwable t ) {  // Catch Throwable rather than Exception (a subclass).
            logger.error("Caught exception in ScheduledExecutorService.", t);
        }
    }
}
