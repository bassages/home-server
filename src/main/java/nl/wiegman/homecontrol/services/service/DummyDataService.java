package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.concurrent.*;

@Component
@Api(value=DummyDataService.SERVICE_PATH, description="Genereert dummy data voor elektriciteitsverbruik")
@Path(DummyDataService.SERVICE_PATH)
public class DummyDataService {

    public static final String SERVICE_PATH = "dummydata";
    public static final int INTERVAL_IN_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(DummyDataService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> scheduledFuture = null;

    private int lastGenerated = 50;

    @Autowired
    private MeterstandService meterstandService;

    @PostConstruct
    @POST
    @Path("start")
    public void start() {
        loadInitialData();
        if (scheduledFuture == null) {
            scheduledFuture = scheduler.scheduleAtFixedRate(this::storeDummyData, 0, INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
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

    private void loadInitialData() {
        long before = Runtime.getRuntime().totalMemory();
        logger.info("Memory usage before generating dummy data: " + FileUtils.byteCountToDisplaySize(before));

        long timestamp = System.currentTimeMillis() - (TimeUnit.SECONDS.toMillis(INTERVAL_IN_SECONDS) * MeterstandenStore.MAX_NR_OF_ITEMS);

        for (int i=0; i< MeterstandenStore.MAX_NR_OF_ITEMS; i++) {
            meterstandService.opslaanMeterstand(timestamp, getDummyVermogenInWatt(), 0, 0, 0);
            timestamp += TimeUnit.SECONDS.toMillis(INTERVAL_IN_SECONDS);
        }

        long after = Runtime.getRuntime().totalMemory();
        logger.info("Memory usage after generating dummy data: " + FileUtils.byteCountToDisplaySize(before));
        logger.info("Memory usage difference: " + FileUtils.byteCountToDisplaySize(after-before));
    }

    private int getDummyVermogenInWatt() {
        int min = ThreadLocalRandom.current().nextInt(50, lastGenerated + 1);
        int max = ThreadLocalRandom.current().nextInt(lastGenerated, 1200 + 1);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void storeDummyData() {
        meterstandService.opslaanMeterstand(System.currentTimeMillis(), getDummyVermogenInWatt(), 0, 0, 0);
    }
}
