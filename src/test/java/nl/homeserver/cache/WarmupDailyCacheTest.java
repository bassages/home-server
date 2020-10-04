package nl.homeserver.cache;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WarmupDailyCacheTest {

    private static final String FIELDNAME_WARMUP_CACHE_DAILY = "warmupCacheDaily";

    private WarmupDailyCache warmupDailyCache;

    @Mock
    private DailyCacheWarmer dailyCacheWarmer;

    @Before
    public void setUp() {
        warmupDailyCache = new WarmupDailyCache(List.of(dailyCacheWarmer));
    }

    @Test
    public void givenWarmupDisabledWhenConsiderDailyWarmupThenNoWarmup() {
        setWarmupCacheDisabled();

        warmupDailyCache.considerDailyWarmup();

        verifyNoMoreInteractions(dailyCacheWarmer);
    }

    @Test
    public void givenWarmupEnabledWhenConsiderDailyWarmupThenWarmup() {
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