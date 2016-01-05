package nl.wiegman.homecontrol.services.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Component
public class CacheLoader {

    private final Logger logger = LoggerFactory.getLogger(CacheLoader.class);

    @Inject
    private ElektriciteitService elektriciteitService;

    @Async
    public void loadCacheAsync() {
//        logger.info("Start pre loading caches verbruik per maand in jaar");
//        elektriciteitService.getVerbruikPerMaandInJaar(LocalDate.now().getYear());
//        elektriciteitService.getVerbruikPerMaandInJaar(LocalDate.now().getYear() - 1);
//        logger.info("Finished pre loading caches verbruik per maand per jaar");
//
//        logger.info("Start pre loading caches verbruik per dag");
//        elektriciteitService.getVerbruikPerDag(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(356), System.currentTimeMillis());
//        logger.info("Finished pre loading caches verbruik per dag");
    }
}
