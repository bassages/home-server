package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
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
public class HistoricDataGeneratorService {

    private final Logger logger = LoggerFactory.getLogger(HistoricDataGeneratorService.class);

    public static final String SERVICE_PATH = "historicdatagenerator";

    public static final int GENERATOR_RUN_INTERVAL_IN_SECONDS = 2;

    public static final double STROOM_VERBRUIK_PER_INTERVAL = 0.001d;

    private final ScheduledExecutorService historischeDataGeneratorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> historischeDataGenerator = null;

    private int lastGeneratedOpgenomenVermogen = 50;

    private Double lastGeneratedStroomTarief1 = null;
    private Double lastGeneratedStroomTarief2 = null;
    private Long lastGeneratedTimestamp = null;

    @Autowired
    private MeterstandService meterstandService;

    @Autowired
    private MeterstandRepository meterstandRepository;

    @PostConstruct
    public void autoStart() {
        Meterstand oldest = meterstandService.getOldest();
        if (oldest == null) {
            lastGeneratedStroomTarief1 = 100000d;
            lastGeneratedStroomTarief2 = 100000d;
            lastGeneratedTimestamp = System.currentTimeMillis();
        } else {
            lastGeneratedStroomTarief1 = (double)oldest.getStroomTarief1();
            lastGeneratedStroomTarief2 = (double)oldest.getStroomTarief2();
            lastGeneratedTimestamp = oldest.getDatumtijd();
        }
        startGeneratingHistoricData();
    }

    @POST
    @Path("startGeneratingHistoricData")
    public void startGeneratingHistoricData() {
        if (historischeDataGenerator == null) {
            historischeDataGenerator = historischeDataGeneratorScheduler.scheduleAtFixedRate(this::generateHistoricData, 0, GENERATOR_RUN_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
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

    private int getDummyVermogenInWatt() {
        int min = ThreadLocalRandom.current().nextInt(50, lastGeneratedOpgenomenVermogen + 1);
        int max = ThreadLocalRandom.current().nextInt(lastGeneratedOpgenomenVermogen, 1200 + 1);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void generateHistoricData() {
        lastGeneratedTimestamp -= TimeUnit.SECONDS.toMillis(10);
        lastGeneratedStroomTarief1 += STROOM_VERBRUIK_PER_INTERVAL;
        lastGeneratedStroomTarief2 += STROOM_VERBRUIK_PER_INTERVAL;

        Meterstand meterstand = new Meterstand();
        meterstand.setDatumtijd(lastGeneratedTimestamp);
        meterstand.setStroomOpgenomenVermogenInWatt(getDummyVermogenInWatt());
        meterstand.setGas(0);
        meterstand.setStroomTarief1((int)lastGeneratedStroomTarief2.doubleValue());
        meterstand.setStroomTarief2((int)lastGeneratedStroomTarief1.doubleValue());

        logger.info("Add historic data for " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(lastGeneratedTimestamp)));
        meterstandRepository.save(meterstand);
    }
}
