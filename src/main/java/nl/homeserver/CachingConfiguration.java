package nl.homeserver;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.Map;

import static org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration;

@Profile("!test") // no caching during (integration)tests
@Configuration
@EnableCaching
public class CachingConfiguration {

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

    // IntelliJ: "Method '...' is never used"
    // Ignore because: actually it IS used by Spring because it is annotated with @Bean
    @SuppressWarnings("unused")
    @Bean
    public CacheManager ehCacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        CACHES.forEach((name, maxNrOfHeapEntries) -> cacheManager.createCache(name, fromEhcacheCacheConfiguration(
                heapCacheConfigBuiler(maxNrOfHeapEntries))));
        return cacheManager;
    }

    private static CacheConfigurationBuilder<Object, Object> heapCacheConfigBuiler(final int maxNumberOfHeapEntries) {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Object.class,
                        Object.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder().heap(maxNumberOfHeapEntries, EntryUnit.ENTRIES))
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofDays(14)));
    }
}
