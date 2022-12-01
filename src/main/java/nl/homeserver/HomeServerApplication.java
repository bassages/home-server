package nl.homeserver;

import org.apache.http.impl.client.HttpClientBuilder;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
public class HomeServerApplication {

    private static final Map<String, Integer> CACHES = Map.of(
        "energyContractsInPeriod", 20,
        "gasVerbruikInPeriode", 100,
        "stroomVerbruikInPeriode", 100,
        "energieVerbruikInPeriode",100,
        "opgenomenVermogenHistory", 100,
        "meestRecenteMeterstandOpDag",100,
        "klimaatInPeriod", 100,
        "averageKlimaatInMonth", 48,
        "standbyPower", 48
    );

    public static void main(final String[] args) {
        SpringApplication.run(HomeServerApplication.class, args);
    }

    @Bean
    public Executor getTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }

    // IntelliJ: "Method '...' is never used"
    // Ignore because: actually it IS used by Spring because it is annotated with @Bean
    @SuppressWarnings("unused")
    @Bean
    public CacheManager ehCacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        CACHES.forEach((name, maxNrOfHeapEntries) -> cacheManager.createCache(name, fromEhcacheCacheConfiguration(
                twoWeekIdleHeapCacheConfigBuiler(maxNrOfHeapEntries))));
        return cacheManager;
    }

    private static CacheConfigurationBuilder<Object, Object> twoWeekIdleHeapCacheConfigBuiler(final int maxNumberOfHeapEntries) {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Object.class,
                        Object.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder().heap(maxNumberOfHeapEntries, EntryUnit.ENTRIES))
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofDays(14)));
    }

    @Bean
    @Scope(value = SCOPE_PROTOTYPE)
    public HttpClientBuilder getHttpClientBuilder() {
        return HttpClientBuilder.create();
    }
}
