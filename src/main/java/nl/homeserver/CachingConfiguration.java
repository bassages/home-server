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

    public static final String CACHE_NAME_AVERAGE_CLIMATE_IN_MONTH = "averageClimateInMonth";
    public static final String CACHE_NAME_CLIMATE_IN_PERIOD = "climateInPeriod";
    public static final String CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG = "meestRecenteMeterstandOpDag";
    public static final String CACHE_NAME_STANDBY_POWER = "standbyPower";
    public static final String CACHE_NAME_GAS_VERBRUIK_IN_PERIODE = "gasVerbruikInPeriode";
    public static final String CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE = "stroomVerbruikInPeriode";
    public static final String CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY = "opgenomenVermogenHistory";

    private static final Map<String, Integer> CACHES = Map.of(
            "energyContractsInPeriod", 20,
            CACHE_NAME_GAS_VERBRUIK_IN_PERIODE, 100,
            CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE, 100,
            CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY, 100,
            CACHE_NAME_MEEST_RECENTE_METERSTAND_OP_DAG,100,
            CACHE_NAME_CLIMATE_IN_PERIOD, 100,
            CACHE_NAME_AVERAGE_CLIMATE_IN_MONTH, 48,
            CACHE_NAME_STANDBY_POWER, 48
    );

    // IntelliJ: "Method '...' is never used"
    // Ignore because: actually it IS used by Spring because it is annotated with @Bean
    @SuppressWarnings("unused")
    @Bean
    public CacheManager ehCacheManager() {
        final CachingProvider provider = Caching.getCachingProvider();
        final CacheManager cacheManager = provider.getCacheManager();
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
