package nl.homeserver.cache;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@RunWith(MockitoJUnitRunner.class)
public class WarmupInitialCacheTest {

    private static final String FIELDNAME_WARMUP_CACHE_ON_APPLICATION_START = "warmupCacheOnApplicationStart";

    private WarmupInitialCache warmupInitialCache;

    @Mock
    private InitialCacheWarmer initialCacheWarmer;

    @Before
    public void setUp() {
        warmupInitialCache = new WarmupInitialCache(List.of(initialCacheWarmer));
    }

    @Test
    public void givenWarmupDisabledWhenApplicationStartedThenNoWarmup() {
        setWarmupCacheDisabled();

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verifyNoMoreInteractions(initialCacheWarmer);
    }

    @Test
    public void givenWarmupEnabledWhenApplicationStartedThenWarmup() {
        setWarmupCacheEnabled();

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(initialCacheWarmer).warmupInitialCache();
    }

    private void setWarmupCacheDisabled() {
        setField(warmupInitialCache, FIELDNAME_WARMUP_CACHE_ON_APPLICATION_START, false);
    }

    private void setWarmupCacheEnabled() {
        setField(warmupInitialCache, FIELDNAME_WARMUP_CACHE_ON_APPLICATION_START, true);
    }
}