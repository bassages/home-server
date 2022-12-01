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
class WarmupCacheOnStartupTest {

    private static final String FIELDNAME_WARMUP_CACHE_ON_APPLICATION_START = "warmupCacheOnApplicationStart";

    WarmupCacheOnStartup warmupCacheOnStartup;

    @Mock
    StartupCacheWarmer startupCacheWarmer;

    @BeforeEach
    void setUp() {
        warmupCacheOnStartup = new WarmupCacheOnStartup(List.of(startupCacheWarmer));
    }

    @Test
    void givenWarmupDisabledWhenApplicationStartedThenNoWarmup() {
        setWarmupCacheDisabled();

        warmupCacheOnStartup.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verifyNoMoreInteractions(startupCacheWarmer);
    }

    @Test
    void givenWarmupEnabledWhenApplicationStartedThenWarmup() {
        setWarmupCacheEnabled();

        warmupCacheOnStartup.onApplicationEvent(mock(ApplicationReadyEvent.class));

        verify(startupCacheWarmer).warmupCacheOnStartup();
    }

    private void setWarmupCacheDisabled() {
        setField(warmupCacheOnStartup, FIELDNAME_WARMUP_CACHE_ON_APPLICATION_START, false);
    }

    private void setWarmupCacheEnabled() {
        setField(warmupCacheOnStartup, FIELDNAME_WARMUP_CACHE_ON_APPLICATION_START, true);
    }
}
