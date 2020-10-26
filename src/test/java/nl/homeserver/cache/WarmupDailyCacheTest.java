package nl.homeserver.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class WarmupDailyCacheTest {

    private static final String FIELDNAME_WARMUP_CACHE_DAILY = "warmupCacheDaily";

    WarmupDailyCache warmupDailyCache;

    @Mock
    DailyCacheWarmer dailyCacheWarmer;

    @BeforeEach
    void setUp() {
        warmupDailyCache = new WarmupDailyCache(List.of(dailyCacheWarmer));
    }

    @Test
    void givenWarmupDisabledWhenConsiderDailyWarmupThenNoWarmup() {
        setWarmupCacheDisabled();

        warmupDailyCache.considerDailyWarmup();

        verifyNoMoreInteractions(dailyCacheWarmer);
    }

    @Test
    void givenWarmupEnabledWhenConsiderDailyWarmupThenWarmup() {
        setWarmupCacheEnabled();

        warmupDailyCache.considerDailyWarmup();

        verify(dailyCacheWarmer).warmupDailyCache();
    }


    private void setWarmupCacheDisabled() {
        setField(warmupDailyCache, FIELDNAME_WARMUP_CACHE_DAILY, false);
    }

    private void setWarmupCacheEnabled() {
        setField(warmupDailyCache, FIELDNAME_WARMUP_CACHE_DAILY, true);
    }
}
