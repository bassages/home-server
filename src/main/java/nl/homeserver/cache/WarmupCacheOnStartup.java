package nl.homeserver.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
class WarmupCacheOnStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final List<StartupCacheWarmer> startupCacheWarmers;

    @Value("${home-server.cache.warmup.on-application-start}")
    private boolean warmupCacheOnApplicationStart;

    WarmupCacheOnStartup(final List<StartupCacheWarmer> startupCacheWarmers) {
        this.startupCacheWarmers = startupCacheWarmers;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (warmupCacheOnApplicationStart) {
            log.info("Warmup cache start");
            startupCacheWarmers.forEach(StartupCacheWarmer::warmupCacheOnStartup);
            log.info("Warmup cache completed");
        }
    }
}
