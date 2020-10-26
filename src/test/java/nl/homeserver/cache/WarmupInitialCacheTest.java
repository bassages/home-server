package nl.homeserver.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class WarmupInitialCacheTest {

    private static final String FIELDNAME_WARMUP_CACHE_ON_APPLICATION_START = "warmupCacheOnApplicationStart";

    WarmupInitialCache warmupInitialCache;

    @Mock
    InitialCacheWarmer initialCacheWarmer;

    @BeforeEach
    void setUp() {
        warmupInitialCache = new WarmupInitialCache(List.of(initialCacheWarmer));
    }

    @Test
    void givenWarmupDisabledWhenApplicationStartedThenNoWarmup() {
        setWarmupCacheDisabled();

        warmupInitialCache.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verifyNoMoreInteractions(initialCacheWarmer);
    }

    @Test
    void givenWarmupEnabledWhenApplicationStartedThenWarmup() {
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
