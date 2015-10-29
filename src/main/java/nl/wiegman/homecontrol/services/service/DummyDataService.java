package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Api(value=DummyDataService.SERVICE_PATH, description="Onvangt en verspreid informatie over het elektriciteitsverbruik")
@Path(DummyDataService.SERVICE_PATH)
public class DummyDataService {

    public static final String SERVICE_PATH = "dummydata";
    public static final int INTERVAL_IN_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(DummyDataService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> scheduledFuture = null;

    private boolean oplopend = true;
    private int opgenomenVermogen = 0;

    @Autowired
    private ElektriciteitService elektriciteitService;

    @PostConstruct
    @POST
    @Path("start")
    public void start() {
        loadInitialData();
        if (scheduledFuture == null) {
            scheduledFuture = scheduler.scheduleAtFixedRate(this::sendDummyData, 0, INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
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

        long timestamp = System.currentTimeMillis();
        for (int i=0; i<ElectriciteitStore.MAX_NR_OF_ITEMS; i++) {
            elektriciteitService.opslaanAfgenomenVermogen(getDummyVermogenInWatt(), timestamp);
            timestamp -= TimeUnit.SECONDS.toMillis(INTERVAL_IN_SECONDS);
        }

        long after = Runtime.getRuntime().totalMemory();
        logger.info("Memory usage after generating dummy data: " + FileUtils.byteCountToDisplaySize(before));
        logger.info("Memory usage difference: " + FileUtils.byteCountToDisplaySize(after-before));
    }

    private int getDummyVermogenInWatt() {
        return ThreadLocalRandom.current().nextInt(50, 1000 + 1);
    }

    private void sendDummyData() {
        elektriciteitService.opslaanAfgenomenVermogen(getDummyVermogenInWatt(), System.currentTimeMillis());
    }
}
