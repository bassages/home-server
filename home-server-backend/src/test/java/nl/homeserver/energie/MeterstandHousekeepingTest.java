package nl.homeserver.energie;

import static ch.qos.logback.classic.Level.INFO;
import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static nl.homeserver.energie.MeterstandBuilder.aMeterstand;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static nl.homeserver.util.TimeMachine.useSystemDefaultClock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.LoggingRule;
import nl.homeserver.MessageContaining;
import nl.homeserver.cache.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class MeterstandHousekeepingTest {

    @InjectMocks
    private MeterstandHousekeeping meterstandHousekeeping;

    @Mock
    private MeterstandRepository meterstandRepository;
    @Mock
    private CacheService cacheService;
    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<List<Meterstand>> deletedMeterstandCaptor;

    @Rule
    public LoggingRule loggingRule = new LoggingRule(MeterstandHousekeeping.class);

    @Test
    public void whenCleanUpThenCachesCleared() {
        useSystemDefaultClock(clock);

        meterstandHousekeeping.dailyCleanup();

        verify(cacheService).clear(VerbruikKostenOverzichtService.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
        verify(cacheService).clear(VerbruikKostenOverzichtService.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
    }

    @Test
    public void givenOnlyASingleMeterstandExistsWhenCleanupThenNoneDeleted() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        timeTravelTo(clock, dayToCleanup.plusDays(1).atStartOfDay());

        Meterstand meterstand = aMeterstand().withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        when(meterstandRepository.findByDateTimeBetween(any(), any())).thenReturn(singletonList(meterstand));

        meterstandHousekeeping.dailyCleanup();

        verify(meterstandRepository, times(3)).findByDateTimeBetween(any(), any());
        verifyNoMoreInteractions(meterstandRepository);
    }

    @Test
    public void whenCleanupThenAllButFirstAndLastMeterstandPerHourAreDeleted() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        timeTravelTo(clock, dayToCleanup.plusDays(1).atStartOfDay());

        Meterstand meterstand1 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        Meterstand meterstand2 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        Meterstand meterstand3 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 30, 0)).build();
        Meterstand meterstand4 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 45, 0)).build();

        Meterstand meterstand5 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 0, 0)).build();
        Meterstand meterstand6 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 15, 0)).build();
        Meterstand meterstand7 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 30, 0)).build();
        Meterstand meterstand8 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 45, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(asList(meterstand1, meterstand2, meterstand3, meterstand4, meterstand5, meterstand6, meterstand7, meterstand8));

        meterstandHousekeeping.dailyCleanup();

        verify(meterstandRepository, times(2)).deleteInBatch(deletedMeterstandCaptor.capture());

        assertThat(deletedMeterstandCaptor.getAllValues().get(0)).containsExactly(meterstand2, meterstand3);
        assertThat(deletedMeterstandCaptor.getAllValues().get(1)).containsExactly(meterstand6, meterstand7);
    }

    @Test
    public void whenCleanupThenKeptAndDeletedMeterstandenAreLogged() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        timeTravelTo(clock, dayToCleanup.plusDays(1).atStartOfDay());

        Meterstand meterstand1 = aMeterstand().withId(1).withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        Meterstand meterstand2 = aMeterstand().withId(2).withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        Meterstand meterstand3 = aMeterstand().withId(3).withDateTime(dayToCleanup.atTime(12, 30, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(asList(meterstand1, meterstand2, meterstand3));

        loggingRule.setLevel(INFO);

        meterstandHousekeeping.dailyCleanup();

        List<LoggingEvent> loggedEvents = loggingRule.getLoggedEventCaptor().getAllValues();
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[INFO] Keep first in hour 12: Meterstand[id=1"));
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[INFO] Keep last in hour 12: Meterstand[id=3"));
        assertThat(loggedEvents).haveExactly(1, new MessageContaining("[INFO] Delete: Meterstand[id=2"));
    }

    @Test
    public void givenLogLevelIsOffWhenCleanupThenNothingLogged() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        timeTravelTo(clock, dayToCleanup.plusDays(1).atStartOfDay());

        Meterstand meterstand1 = aMeterstand().withId(1).withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        Meterstand meterstand2 = aMeterstand().withId(2).withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        Meterstand meterstand3 = aMeterstand().withId(3).withDateTime(dayToCleanup.atTime(12, 30, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(asList(meterstand1, meterstand2, meterstand3));

        loggingRule.setLevel(Level.OFF);

        meterstandHousekeeping.dailyCleanup();

        assertThat(loggingRule.getLoggedEventCaptor().getAllValues()).isEmpty();
    }
}