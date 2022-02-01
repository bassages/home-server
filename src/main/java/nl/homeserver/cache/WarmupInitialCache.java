package nl.homeserver.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
class WarmupInitialCache implements ApplicationListener<ApplicationReadyEvent> {

    private final List<InitialCacheWarmer> initialCacheWarmers;

    @Value("${cache.warmup.on-application-start}")
    private boolean warmupCacheOnApplicationStart;

    WarmupInitialCache(final List<InitialCacheWarmer> initialCacheWarmers) {
        this.initialCacheWarmers = initialCacheWarmers;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (warmupCacheOnApplicationStart) {
            log.info("Warmup cache start");
            initialCacheWarmers.forEach(InitialCacheWarmer::warmupInitialCache);
            log.info("Warmup cache completed");
        }
    }
}
