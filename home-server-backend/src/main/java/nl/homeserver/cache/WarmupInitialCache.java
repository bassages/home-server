package nl.homeserver.cache;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
class WarmupInitialCache implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = getLogger(WarmupInitialCache.class);

    private final List<InitialCacheWarmer> initialCacheWarmers;

    @Value("${cache.warmup.on-application-start}")
    private boolean warmupCacheOnApplicationStart;

    WarmupInitialCache(final List<InitialCacheWarmer> initialCacheWarmers) {
        this.initialCacheWarmers = initialCacheWarmers;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (warmupCacheOnApplicationStart) {
            LOGGER.info("Warmup cache start");
            initialCacheWarmers.forEach(InitialCacheWarmer::warmupInitialCache);
            LOGGER.info("Warmup cache completed");
        }
    }
}
