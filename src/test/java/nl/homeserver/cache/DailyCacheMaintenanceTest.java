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
class DailyCacheMaintenanceTest {

    static final String FIELDNAME_IS_WARMUP_CACHE_DAILY_ENABLED = "isDailyCacheMaintenanceEnabled";

    DailyCacheMaintenance dailyCacheMaintenance;

    @Mock
    DailyCacheMaintainer dailyCacheMaintainer;

    @BeforeEach
    void setUp() {
        dailyCacheMaintenance = new DailyCacheMaintenance(List.of(dailyCacheMaintainer));
    }

    @Test
    void givenWarmupDisabledWhenConsiderDailyWarmupThenNoWarmup() {
        // given
        disableDailyCacheWarmup();

        // when
        dailyCacheMaintenance.considerDailyWarmup();

        // then
        verifyNoMoreInteractions(dailyCacheMaintainer);
    }

    @CaptureLogging(DailyCacheMaintenance.class)
    @Test
    void givenWarmupEnabledWhenConsiderDailyWarmupThenWarmup(final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {
        // given
        enableDailyCachWarmup();

        // when
        dailyCacheMaintenance.considerDailyWarmup();

        // then
        verify(dailyCacheMaintainer).maintainCacheDaily();
        assertThat(loggerEventCaptor.getAllValues())
                .haveExactly(1, new ContainsMessageAtLevel("Maintaining cache start", INFO))
                .haveExactly(1, new ContainsMessageAtLevel("Maintaining cache completed", INFO));

    }

    private void disableDailyCacheWarmup() {
        setField(dailyCacheMaintenance, FIELDNAME_IS_WARMUP_CACHE_DAILY_ENABLED, false);
    }

    private void enableDailyCachWarmup() {
        setField(dailyCacheMaintenance, FIELDNAME_IS_WARMUP_CACHE_DAILY_ENABLED, true);
    }
}
