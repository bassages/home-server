package nl.homeserver.cache;

import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.CaptureLogging;
import nl.homeserver.ContainsMessageAtLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static ch.qos.logback.classic.Level.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class WarmupCacheDailyTest {

    static final String FIELDNAME_WARMUP_CACHE_DAILY = "warmupCacheDaily";

    WarmupCacheDaily warmupCacheDaily;

    @Mock
    DailyCacheWarmer dailyCacheWarmer;

    @BeforeEach
    void setUp() {
        warmupCacheDaily = new WarmupCacheDaily(List.of(dailyCacheWarmer));
    }

    @Test
    void givenWarmupDisabledWhenConsiderDailyWarmupThenNoWarmup() {
        // given
        disableDailyCacheWarmup();

        // when
        warmupCacheDaily.considerDailyWarmup();

        // then
        verifyNoMoreInteractions(dailyCacheWarmer);
    }

    @CaptureLogging(WarmupCacheDaily.class)
    @Test
    void givenWarmupEnabledWhenConsiderDailyWarmupThenWarmup(final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {
        // given
        enableDailyCachWarmup();

        // when
        warmupCacheDaily.considerDailyWarmup();

        // then
        verify(dailyCacheWarmer).warmupCacheDaily();
        assertThat(loggerEventCaptor.getAllValues())
                .haveExactly(1, new ContainsMessageAtLevel("Warmup cache start", INFO))
                .haveExactly(1, new ContainsMessageAtLevel("Warmup cache completed", INFO));

    }

    private void disableDailyCacheWarmup() {
        setField(warmupCacheDaily, FIELDNAME_WARMUP_CACHE_DAILY, false);
    }

    private void enableDailyCachWarmup() {
        setField(warmupCacheDaily, FIELDNAME_WARMUP_CACHE_DAILY, true);
    }
}
