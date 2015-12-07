package nl.wiegman.homecontrol.services.service;

import io.swagger.annotations.Api;
import nl.wiegman.homecontrol.services.model.api.Meterstand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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

    private int lastGeneratedOpgenomenVermogen = 50;
    private double lastGeneratedStroomTarief1 = 100000d;
    private double lastGeneratedStroomTarief2 = 100000d;

    @Autowired
    private MeterstandService meterstandService;

    @PostConstruct
    @POST
    @Path("start")
    public void start() {
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

    @Async
    public void generateHistoricData() {

        Meterstand mostRecent = null;
        while(mostRecent == null) {
            mostRecent = meterstandService.getMostRecent();
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int tarief1 = mostRecent.getStroomTarief1();
        int tarief2 = mostRecent.getStroomTarief2();
        long timestamp = mostRecent.getDatumtijd();

//        while (1==1) {
//            timestamp -= TimeUnit.SECONDS.toMillis(INTERVAL_IN_SECONDS);
//            tarief1 -= 0.02;
//            tarief2 -= 0.02;
//            meterstandService.opslaanMeterstand(timestamp, getDummyVermogenInWatt(), tarief1, tarief2, 0);
//        }
    }

    private int getDummyVermogenInWatt() {
        int min = ThreadLocalRandom.current().nextInt(50, lastGeneratedOpgenomenVermogen + 1);
        int max = ThreadLocalRandom.current().nextInt(lastGeneratedOpgenomenVermogen, 1200 + 1);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void storeDummyData() {
        meterstandService.opslaanMeterstand(System.currentTimeMillis(), getDummyVermogenInWatt(), getStroomTarief1(), getStroomTarief2(), 0);
    }

    private int getStroomTarief2() {
        lastGeneratedStroomTarief2 += 0.02d;
        return (int)lastGeneratedStroomTarief2;
    }

    private int getStroomTarief1() {
        lastGeneratedStroomTarief1 += 0.02d;
        return (int)lastGeneratedStroomTarief1;
    }
}
